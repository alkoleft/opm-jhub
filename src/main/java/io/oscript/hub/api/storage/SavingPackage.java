package io.oscript.hub.api.storage;

import io.oscript.hub.api.integration.PackageType;
import io.oscript.hub.api.integration.VersionSourceInfo;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.utils.Constants;

public class SavingPackage {

    OspxPackage packageData;
    VersionSourceInfo sourceInfo;
    String channel;
    PackageType type;

    public SavingPackage(OspxPackage packageData) {
        this(packageData, PackageType.UNKNOWN, VersionSourceInfo.UNKNOWN, Constants.STABLE);
    }

    public SavingPackage(OspxPackage packageData, String channel) {
        this(packageData, PackageType.UNKNOWN, VersionSourceInfo.UNKNOWN, channel);
    }

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
}
