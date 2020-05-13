package io.oscript.hub.api.storage;

import io.oscript.hub.api.integration.PackageType;
import io.oscript.hub.api.integration.VersionSourceInfo;
import io.oscript.hub.api.ospx.OspxPackage;

public class SavingPackage {

    private final OspxPackage packageData;
    private final VersionSourceInfo sourceInfo;
    private final String channel;
    private final PackageType type;

    public SavingPackage(OspxPackage packageData, PackageType type, VersionSourceInfo sourceInfo, String channel) {
        this.packageData = packageData;
        this.sourceInfo = sourceInfo;
        this.channel = channel;
        this.type = type;
    }

    public String getName() {
        return packageData.getMetadata().getName();
    }

    public String getVersion() {
        return packageData.getMetadata().getVersion();
    }

    public String getChannel() {
        return channel;
    }

    public OspxPackage getPackageData() {
        return packageData;
    }

    public VersionSourceInfo getSourceInfo() {
        return sourceInfo;
    }

    public PackageType getType() {
        return type;
    }
}
