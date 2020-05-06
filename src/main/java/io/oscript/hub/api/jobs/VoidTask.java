package io.oscript.hub.api.jobs;

import io.oscript.hub.api.integration.PackagesSource;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@EnableAsync
public class VoidTask extends SimpleTask<Void> {

    private final PackagesSource worker;

    public VoidTask(PackagesSource worker) {
        this.worker = worker;
    }

    @Override
    public CompletableFuture<Void> run() {

        return CompletableFuture.supplyAsync(() -> {
            try {
                worker.sync();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            return null;
        });
    }

    @Override
    public String getName() {
        return worker.getClass().getSimpleName();
    }
}
