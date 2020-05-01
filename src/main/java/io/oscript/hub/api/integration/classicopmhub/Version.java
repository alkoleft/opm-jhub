package io.oscript.hub.api.integration.classicopmhub;

import io.oscript.hub.api.integration.PackageSourceType;
import io.oscript.hub.api.integration.VersionBase;
import lombok.Data;

import java.io.InputStream;

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

    public PackageSourceType getType() {
        return PackageSourceType.BinaryPackage;
    }

    public String getPackageID() {
        return null;
    }

    public InputStream getStream() {
        return null;
    }
}
