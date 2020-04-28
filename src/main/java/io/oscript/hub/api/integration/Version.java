package io.oscript.hub.api.integration;

import lombok.Data;

@Data
public class Version {
    protected String name;
    protected String downloadURL;
    protected String url;

    public Version(String name, String downloadURL) {
        this.name = name;
        this.downloadURL = downloadURL;
    }
}