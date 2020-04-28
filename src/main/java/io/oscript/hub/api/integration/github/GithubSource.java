package io.oscript.hub.api.integration.github;

public class GithubSource {
    GithubSourceType type;
    String value;

    public GithubSource(GithubSourceType type, String value) {
        this.type = type;
        this.value = value;
    }
}
