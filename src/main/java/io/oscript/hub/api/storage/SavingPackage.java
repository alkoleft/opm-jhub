package io.oscript.hub.api.storage;

import io.oscript.hub.api.integration.VersionSourceInfo;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.utils.Constants;

public class SavingPackage {
    OspxPackage packageData;
    VersionSourceInfo sourceInfo;
    String channel;

    public SavingPackage(OspxPackage packageData) {
        this(packageData, VersionSourceInfo.UNKNOWN, Constants.defaultChannel);
    }

    public SavingPackage(OspxPackage packageData, String channel) {
        this(packageData, VersionSourceInfo.UNKNOWN, channel);
    }

    public SavingPackage(OspxPackage packageData, VersionSourceInfo sourceInfo, String channel) {
        this.packageData = packageData;
        this.sourceInfo = sourceInfo;
        this.channel = channel;
    }

}
