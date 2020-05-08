package io.oscript.hub.api.integration.classicopmhub;

import io.oscript.hub.api.integration.VersionBase;
import lombok.Data;

@Data
public class Version implements VersionBase {
    String version;
    String packageID;

    public Version(String version, String packageID) {
        this.version = version;
        this.packageID = packageID;
    }

    String fullName() {
        return String.format("%s@%s", packageID, version);
    }
}
