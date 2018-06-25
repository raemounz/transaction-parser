package halim.raymond.txnparser.listener;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;


@Component
public class TransactionsJobCompletionListener extends JobExecutionListenerSupport {

    private final JdbcTemplate jdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(TransactionsJobCompletionListener.class);

    @Value("${archive.directory}")
    private String archiveDirectory;

    @Value("${results.directory}")
    private String resultsDirectory;

    @Autowired
    public TransactionsJobCompletionListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            String source = jobExecution.getJobParameters().getString("inputFile");
            String filename = jobExecution.getJobParameters().getString("inputFileName");
            String dateString = filename.substring(filename.indexOf("_") + 1, filename.lastIndexOf("."));
            String target = archiveDirectory + FileSystems.getDefault().getSeparator() + filename;
            String resultName = resultsDirectory + FileSystems.getDefault().getSeparator() + "result_" + dateString + ".csv";
            try {
                Files.move(Paths.get(source), Paths.get(target));
            } catch (IOException e) {
                log.error(String.format("Failed moving file from %s to %s.", source, target));
            }

            jdbcTemplate.update("INSERT INTO daily_acct_txn (date, account, no_of_txn, ccy, total_amt) " +
                    "SELECT date, account, COUNT(account), 'CAD', " +
                    "SUM(IF(ccy = 'CAD', amount, (SELECT amount * rate FROM rates WHERE rates.from_ccy = ccy AND date = CONVERT(?, DATE)))) " +
                    "FROM transactions WHERE date = CONVERT(?, DATE) GROUP BY account", new Object[]{dateString, dateString});
            jdbcTemplate.update("INSERT INTO daily_txn (date, no_of_txn, total_amt, ccy) " +
                    "SELECT date, SUM(no_of_txn), SUM(total_amt), 'CAD' from daily_acct_txn WHERE date = CONVERT(?, DATE)",
                    new Object[]{dateString});

            try {
                Writer writer = Files.newBufferedWriter(Paths.get(resultName));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("DATE", "ACCOUNT", "NO_OF_TXN", "TOTAL_AMT", "CCY"));
                jdbcTemplate.query("SELECT date_format(date, '%Y-%m-%d'), account, no_of_txn, " +
                                "total_amt, ccy FROM daily_acct_txn WHERE date = CONVERT(?, DATE) " +
                                "UNION SELECT date_format(date, '%Y-%m-%d'), NULL, no_of_txn, " +
                                "total_amt, ccy FROM daily_txn WHERE date = CONVERT(?, DATE)",
                        new Object[]{dateString, dateString},
                        (rs, rownum) -> {
                            try {
                                csvPrinter.printRecord(rs.getString(1),
                                        rs.getInt(2),
                                        rs.getInt(3),
                                        rs.getDouble(4),
                                        rs.getString(5));
                                csvPrinter.flush();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                            return null;
                });
            } catch (IOException e) {
                log.error(String.format("Failed writing results to %s", resultName));
            }
        }
    }

}