package io.oscript.hub.api.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TaskRunner {

    @Autowired
    GithubSyncTask githubTask;

    public CompletableFuture gitHubSync() {
        return githubTask.sync();
    }
}
