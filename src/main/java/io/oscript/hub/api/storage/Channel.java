package io.oscript.hub.api.storage;

import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.VersionComparator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

public class Channel {

    @Autowired
    IStoreProvider storeProvider;

    ChannelInfo channelInfo;

    public Channel(ChannelInfo info) {
        this.channelInfo = info;
    }

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public List<StoredPackageInfo> getPackages() throws IOException {
        return storeProvider.getPackages(channelInfo.name);
    }

    public StoredPackageInfo getPackage(String name) {
        return storeProvider.getPackage(channelInfo.name, name);
    }

    public StoredVersionInfo pushPackage(SavingPackage pack) {

        if (!(storeProvider.saveVersionBin(pack) && storeProvider.saveVersion(pack))) {
            return null;
        }

        StoredVersionInfo versionInfo = getVersion(pack.getName(), pack.getVersion());
        StoredPackageInfo packageInfo = getPackage(pack.getName());

        if (versionInfo == null) {
            return null;
        }

        if (packageInfo == null) {
            packageInfo = StoredPackageInfo.create(versionInfo.getMetadata());
            storeProvider.savePackage(channelInfo.name, packageInfo);
        } else if (VersionComparator.large(versionInfo.getVersion(), packageInfo.getVersion())) {
            packageInfo.setMetadata(versionInfo.getMetadata());
            storeProvider.savePackage(channelInfo.name, packageInfo);
        }
        return versionInfo;

    }

    // region Versions

    public List<StoredVersionInfo> getVersions(String name) throws IOException {
        return storeProvider.getVersions(channelInfo.name, name);
    }

    public StoredVersionInfo getVersion(String name, String version) {
        if (Common.isNullOrEmpty(version) || version.equalsIgnoreCase("latest")) {
            version = getPackage(name).getVersion();
        }
        return storeProvider.getVersion(channelInfo.name, name, version);
    }

    public boolean containsVersion(String name, String version) {
        return storeProvider.existVersion(channelInfo.name, name, version);
    }

    public boolean containsPackage(String name) {
        return storeProvider.existPackage(channelInfo.name, name);
    }

    public byte[] getVersionData(String name, String version) throws IOException {
        return storeProvider.getPackageData(channelInfo.name, name, version);
    }
    // endregion
}
