package io.oscript.hub.api.jobs;

import java.util.concurrent.CompletableFuture;

public abstract class SimpleTask<T> {
    public abstract CompletableFuture<T> run();

    public abstract String getName();

}
