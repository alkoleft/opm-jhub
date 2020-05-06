package io.oscript.hub.api.jobs;

import java.util.concurrent.CompletableFuture;

public interface SimpleTask<T> {
    CompletableFuture<T> run();

    String getName();

}
