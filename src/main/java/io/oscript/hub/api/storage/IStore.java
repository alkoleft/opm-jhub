package io.oscript.hub.api.storage;

import io.oscript.hub.api.data.IPackageMetadata;
import io.oscript.hub.api.ospx.OspxPackage;

import java.io.IOException;
import java.util.List;

public interface IStore {
    //region Channels
    List<Channel> getChannels();

    Channel channelRegistration(String name);

    Channel channelRegistration(Channel channel);

    Channel getChannel(String name);

    //endregion

    //region Packages

    List<StoredPackageInfo> getPackages(String channel) throws IOException;

    List<StoredPackageInfo> getPackages() throws IOException;

    StoredPackageInfo getPackage(String packageName);

    StoredPackageInfo getPackage(String packageName, String channel);

    //endregion

    StoredVersionInfo getVersion(String packageName, String version, String channel);

    boolean savePackage(OspxPackage ospxPackage, String channel) throws IOException;

    boolean savePackage(SavingPackage pack);

    byte[] getPackageData(IPackageMetadata metadata, String channel) throws IOException;

    boolean containsVersion(String packageID, String version, String channel);

    boolean containsVersion(String packageID, String version);
}

