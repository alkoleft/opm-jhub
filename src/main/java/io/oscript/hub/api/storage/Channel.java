package io.oscript.hub.api.storage;

import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.Naming;
import io.oscript.hub.api.utils.VersionComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Channel {

    private static final Logger logger = LoggerFactory.getLogger(Channel.class);

    @Autowired
    IStoreProvider storeProvider;

    private final ChannelInfo channelInfo;

    public Channel(ChannelInfo info) {
        this.channelInfo = info;
    }

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public List<StoredPackageInfo> getPackages() throws Exception {
        return storeProvider.getPackages(channelInfo.name);
    }

    public StoredPackageInfo getPackage(String name) throws IOException {
        Naming.checkPackageName(name);

        return storeProvider.getPackage(channelInfo.name, name);
    }

    public StoredVersionInfo pushPackage(SavingPackage pack) {

        Naming.check(pack.getChannel(), pack.getName(), pack.getVersion());

        if (!(storeProvider.saveVersionBin(pack) && storeProvider.saveVersion(pack))) {
            return null;
        }

        StoredVersionInfo versionInfo;
        StoredPackageInfo packageInfo;

        try {
            versionInfo = getVersion(pack.getName(), pack.getVersion());
            packageInfo = getPackage(pack.getName());
        } catch (Exception e) {
            logger.error("Ошибка получения информации о версии пакета");
            versionInfo = null;
            packageInfo = null;
        }

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

    public List<StoredVersionInfo> getVersions(String name) throws Exception {
        Naming.checkPackageName(name);

        return storeProvider.getVersions(channelInfo.name, name)
                .stream()
                .sorted((v1, v2) -> -VersionComparator.compare(v1.getVersion(), v2.getVersion()))
                .collect(Collectors.toList());
    }

    public StoredVersionInfo getVersion(String name, String version) throws IOException {
        Naming.check(name, version);

        if (Common.isNullOrEmpty(version) || version.equalsIgnoreCase("latest")) {
            version = getPackage(name).getVersion();
        }
        return storeProvider.getVersion(channelInfo.name, name, version);
    }

    public boolean containsVersion(String name, String version) {
        Naming.check(name, version);
        return storeProvider.existVersion(channelInfo.name, name, version);
    }

    public boolean containsPackage(String name) {
        Naming.checkPackageName(name);
        return storeProvider.existPackage(channelInfo.name, name);
    }

    public byte[] getVersionData(String name, String version) throws IOException {
        Naming.check(name, version);
        return storeProvider.getPackageData(channelInfo.name, name, version);
    }
    // endregion
}
