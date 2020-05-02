package io.oscript.hub.api.integration;

import lombok.Data;

@Data
public class VersionSourceInfo {

    public final static VersionSourceInfo UNKNOWN = new VersionSourceInfo();

    VersionSourceType type = VersionSourceType.UNKNOWN;
    String versionURL;
    String packageURL;
}
