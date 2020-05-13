package io.oscript.hub.api.integration.classicopmhub;

import io.oscript.hub.api.integration.VersionBase;
import lombok.Data;

@Data
public class PackageVersion implements VersionBase {
    String version;
    String packageID;

    public PackageVersion(String version, String packageID) {
        this.version = version;
        this.packageID = packageID;
    }

    String fullName() {
        return String.format("%s@%s", packageID, version);
    }
}
