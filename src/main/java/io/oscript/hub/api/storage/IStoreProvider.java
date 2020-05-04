package io.oscript.hub.api.storage;

import java.io.IOException;
import java.util.List;

public interface IStoreProvider {
    //region Channels
    List<ChannelInfo> getChannels();

    ChannelInfo channelRegistration(String name);

    ChannelInfo getChannel(String name);

    boolean saveChannel(ChannelInfo channel);

    //endregion

    //region Packages

    List<StoredPackageInfo> getPackages(String channel) throws IOException;

    StoredPackageInfo getPackage(String channel, String packageName);

    boolean savePackage(String channel, StoredPackageInfo pack);

    boolean existPackage(String channel, String name);

    //endregion

    // region Versions

    StoredVersionInfo getVersion(String channel, String name, String version);

    byte[] getPackageData(String channel, String name, String version) throws IOException;

    List<StoredVersionInfo> getVersions(String channel, String name) throws IOException;

    boolean saveVersionBin(SavingPackage pack);

    boolean saveVersion(SavingPackage pack);

    boolean saveVersion(String channel, StoredVersionInfo version);

    boolean existVersion(String channel, String name, String version);

    // endregion

}

