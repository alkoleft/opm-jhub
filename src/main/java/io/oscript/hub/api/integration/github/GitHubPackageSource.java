package io.oscript.hub.api.integration.github;

import java.io.IOException;
import java.util.stream.Stream;

public class GitHubPackageSource {

    public Stream<Release> releases() throws IOException {
        GithubIntegration.findNewReleases();
        return GithubIntegration.getRepositories()
                .stream()
                .map(item -> item.getReleases().stream())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty);
    }
}
