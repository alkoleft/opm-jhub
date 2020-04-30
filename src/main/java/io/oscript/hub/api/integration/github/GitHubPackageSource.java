package io.oscript.hub.api.integration.github;

import io.oscript.hub.api.integration.PackageSource;
import io.oscript.hub.api.integration.PackagesSource;

import java.util.stream.Stream;

public class GitHubPackageSource implements PackagesSource {

    public Stream<PackageSource> releases() {
        GithubIntegration.sync();
        return GithubIntegration.getRepositories()
                .stream()
                .map(item -> item.getReleases().stream())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .map(PackageSource.class::cast);
    }
}
