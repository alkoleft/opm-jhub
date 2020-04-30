package io.oscript.hub.api.jobs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
//@SpringBootTest
//@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BatchConfiguration.class})
public class JobsTest {

//    @Bean
//    public JobLauncherTestUtils jobLauncherTestUtils() {
//        return new JobLauncherTestUtils();
//    }

    @Autowired
    @Qualifier("importUserJob")
    private Job importUserJob;

//    @Autowired
//    private JobLauncherTestUtils jobLauncherTestUtils;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        return paramsBuilder.toJobParameters();
    }

    @Test
    public void givenReferenceOutput_whenJobExecuted_thenSuccess() throws Exception {

        BatchConfiguration conf = new BatchConfiguration();
        // when
        JobExecution jobExecution = new JobLauncherTestUtils().getJobLauncher().run(conf.importUserJob(), defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

    }
}
