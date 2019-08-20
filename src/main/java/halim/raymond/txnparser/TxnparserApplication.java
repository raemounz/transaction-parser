package halim.raymond.txnparser;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

@SpringBootApplication
@EnableBatchProcessing
public class TxnparserApplication {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication.run(TxnparserApplication.class, args);

        WatchService watcher = FileSystems.getDefault().newWatchService();
        String loadDirectory = ctx.getEnvironment().getProperty("load.directory");
        Paths.get(loadDirectory).register(watcher, ENTRY_CREATE);

        JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
        Job importRatesJob = ctx.getBean("importRates", Job.class);
        Job importTransactionsJob = ctx.getBean("importTransactions", Job.class);

        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                break;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                if (event.kind() == ENTRY_CREATE) {
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>)event;
                    Path filename = ev.context();
                    JobParameters jobParameters = new JobParametersBuilder()
                            .addString("inputFile",
                            loadDirectory + File.separator + filename.toString())
                            .addString("inputFileName", filename.toString())
                            .toJobParameters();
                    if (filename.toString().startsWith("rates")) {
                        jobLauncher.run(importRatesJob, jobParameters);
                    } else {
                        jobLauncher.run(importTransactionsJob, jobParameters);
                    }
                }
            }
            key.reset();
		}
	}
}
