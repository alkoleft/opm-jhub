package io.oscript.hub.api.integration;

import lombok.Data;

@Data
public class VersionSourceInfo {

    public static final VersionSourceInfo UNKNOWN = new VersionSourceInfo(VersionSourceType.UNKNOWN);
    public static final VersionSourceInfo MANUAL = new VersionSourceInfo(VersionSourceType.MANUAL_PUSH);

    VersionSourceType type = VersionSourceType.UNKNOWN;
    String versionURL;
    String packageURL;

    public VersionSourceInfo(VersionSourceType type) {
        this.type = type;
    }

    public VersionSourceInfo() {
    }
}
