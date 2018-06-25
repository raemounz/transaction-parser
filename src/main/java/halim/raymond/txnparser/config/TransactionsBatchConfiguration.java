package halim.raymond.txnparser.config;

import halim.raymond.txnparser.listener.TransactionsJobCompletionListener;
import halim.raymond.txnparser.model.Transaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Configuration
public class TransactionsBatchConfiguration {

    @Value("${date.format}")
    private String dateFormat;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> transactionReader(@Value("#{jobParameters[inputFile]}") String inputFile) {
        DateFormat format = new SimpleDateFormat(dateFormat);
        FlatFileItemReader<Transaction> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputFile));
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper<Transaction>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[]{"date", "account", "amount", "ccy"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Transaction>() {{
                setTargetType(Transaction.class);
                setCustomEditors(Stream.of(new AbstractMap.SimpleEntry<>(
                        Date.class,
                        new CustomDateEditor(format, false)
                )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }});
        }});
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<Transaction> transactionWriter() {
        JdbcBatchItemWriter<Transaction> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO transactions (date, account, amount, ccy) " +
                "VALUES (:date, :account, :amount, :ccy)");
        writer.setDataSource(dataSource);
        return writer;
    }

    @Bean
    public Job importTransactions(TransactionsJobCompletionListener listener) {
        return jobBuilderFactory.get("importTransactions")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(transactionsStep1())
                .end()
                .build();
    }

    @Bean
    public Step transactionsStep1() {
        return stepBuilderFactory.get("transactionsStep1")
                .<Transaction, Transaction>chunk(10)
                .reader(transactionReader(null))
                .writer(transactionWriter())
                .build();
    }

}
