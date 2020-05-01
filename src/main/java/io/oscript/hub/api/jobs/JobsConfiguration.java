package io.oscript.hub.api.jobs;

import io.oscript.hub.api.integration.classicopmhub.ClassicHubIntegration;
import io.oscript.hub.api.integration.github.GithubIntegration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class JobsConfiguration {

    @Bean(name = "githubSync")
    public VoidTask githubSyncTask(GithubIntegration worker) {
        return new VoidTask(worker);
    }

    @Bean(name = "opmhubSync")
    public VoidTask opmHubSyncTask(ClassicHubIntegration worker) {
        return new VoidTask(worker);
    }

    @Primary
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("bgJob-");
        executor.initialize();
        return executor;
    }
}
