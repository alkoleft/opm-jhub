package io.oscript.hub.api.integration.classicopmhub;

import io.oscript.hub.api.integration.VersionBase;
import lombok.Data;

@Data
public class Version implements VersionBase {
    String version;
    String server;
    String packageID;

    public Version(String version, String server, String packageID) {
        this.version = version;
        this.server = server;
        this.packageID = packageID;
    }

}
