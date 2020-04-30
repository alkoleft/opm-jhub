package io.oscript.hub.api.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilders;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    //
//    @Bean
//    public void buildPackage() {
//        SimpleAsyncTaskExecutor
//    }
//
//    @Bean
//    public ItemReader<PackageSource> stream() {
//        return new CollectPackageSources(Importer.newReleases());
//    }
//
//    @Bean
//    public BuildPackageProcessor builder() {
//        SimpleAsyncTaskExecutor r;
//        return new BuildPackageProcessor();
//    }
//
//    @Bean
//    public Flow splitFlow() {
//        return new FlowBuilder<SimpleFlow>("splitFlow")
//                .add(flow1(), flow2())
//                .build();
//    }
//
//    @Bean
//    public Job importUserJob(Step step1) {
//        return jobBuilderFactory.get("importUserJob")
//                .incrementer(new RunIdIncrementer())
//                .start(splitFlow())
////                .listener(listener)
//
//                .end()
//                .build();
//    }
//    @Bean
//    public Step step1() {
//        return stepBuilderFactory.get("importPackages").
//                .<PackageSource, OspxPackage> chunk(1)
//                .reader(stream())
//                .processor(builder())
//                .processor(builder())
//                .faultTolerant()
//                .skipLimit(10)
//                .skip(Exception.class)
//                .writer(writer)
//                .build();
//    }


    @Bean
    public Step step1() {
        return stepBuilderFactory
                .get("step1")
                .<Integer, String>chunk(3)
                .reader(new generator())
                .processor(new processor())
                .writer(new writer())
                .build();
    }

    @Bean
    public Job importUserJob() {
        return jobBuilders.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    class generator implements ItemReader<Integer> {

        int value = 0;

        @Override
        public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
            if (value < 10) {
                value++;
            }
            return value;
        }
    }

    class processor implements ItemProcessor<Integer, String> {


        @Override
        public String process(Integer item) throws Exception {
            return item.toString();
        }
    }

    class writer implements ItemWriter<String> {

        @Override
        public void write(List<? extends String> items) throws Exception {
            items.forEach(System.out::println);
        }
    }
}
