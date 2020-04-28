package io.oscript.hub.api.integration.github;

import lombok.Data;

import java.util.Date;

@Data
public class Release {
    Date date;
    String version;
    String tag;

    String zipUrl;
    String packageUrl;

    public void setTag(String tag) {
        this.tag = tag;
        if (version == null) {
            if (tag.startsWith("v")) {
                version = tag.substring(1);
            } else {
                version = tag;
            }
        }
    }
}
