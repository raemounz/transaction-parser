package halim.raymond.txnparser.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;


@Component
public class RatesJobCompletionListener extends JobExecutionListenerSupport {

    private final JdbcTemplate jdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(RatesJobCompletionListener.class);

    @Value("${archive.directory}")
    private String archiveDirectory;

    @Autowired
    public RatesJobCompletionListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            String source = jobExecution.getJobParameters().getString("inputFile");
            String target = archiveDirectory + FileSystems.getDefault().getSeparator() + jobExecution.getJobParameters().getString("inputFileName");;
            try {
                Files.move(Paths.get(source), Paths.get(target));
            } catch (IOException e) {
                log.error(String.format("Failed moving file from %s to %s.", source, target));
            }
        }
    }

}