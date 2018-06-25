package halim.raymond.txnparser.config;

import halim.raymond.txnparser.listener.RatesJobCompletionListener;
import halim.raymond.txnparser.model.Rate;
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
public class RatesBatchConfiguration {

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
    public FlatFileItemReader<Rate> ratesReader(@Value("#{jobParameters[inputFile]}") String inputFile) {
        DateFormat format = new SimpleDateFormat(dateFormat);
        FlatFileItemReader<Rate> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputFile));
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper<Rate>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[]{"date", "from_ccy", "to_ccy", "rate"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Rate>() {{
                setTargetType(Rate.class);
                setCustomEditors(Stream.of(new AbstractMap.SimpleEntry<>(
                        Date.class,
                        new CustomDateEditor(format, false)
                )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }});
        }});
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<Rate> ratesWriter() {
        JdbcBatchItemWriter<Rate> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO rates (date, from_ccy, to_ccy, rate) " +
                "VALUES (:date, :from_ccy, :to_ccy, :rate)");
        writer.setDataSource(dataSource);
        return writer;
    }

    @Bean
    public Job importRates(RatesJobCompletionListener listener) {
        return jobBuilderFactory.get("importRates")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(ratesStep1())
                .end()
                .build();
    }

    @Bean
    public Step ratesStep1() {
        return stepBuilderFactory.get("ratesStep1")
                .<Rate, Rate>chunk(3)
                .reader(ratesReader(null))
                .writer(ratesWriter())
                .build();
    }

}
