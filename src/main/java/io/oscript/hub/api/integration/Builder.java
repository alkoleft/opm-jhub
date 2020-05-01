package io.oscript.hub.api.integration;

import io.oscript.hub.api.integration.classicopmhub.ClassicHubIntegration;
import io.oscript.hub.api.integration.github.GithubIntegration;
import org.springframework.beans.factory.annotation.Autowired;

public class Builder {

    @Autowired
    GithubIntegration github;

    @Autowired
    ClassicHubIntegration opmHub;

    public PackagesSource[] getSources() {
        return new PackagesSource[]{
                github, opmHub
        };
    }

    public void collectPackages() {

    }
}
