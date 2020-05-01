package io.oscript.hub.api.jobs;

public class JobAlreadyExecuting extends Exception {
    public JobAlreadyExecuting() {
    }

    public JobAlreadyExecuting(String message) {
        super(message);
    }
}
