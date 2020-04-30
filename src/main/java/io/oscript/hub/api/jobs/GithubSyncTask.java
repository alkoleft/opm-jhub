package io.oscript.hub.api.jobs;

import io.oscript.hub.api.integration.github.GithubIntegration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

@Bean
public class GithubSyncTask implements Runnable {

    @Async
    CompletableFuture<Boolean> sync() {
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }
}
