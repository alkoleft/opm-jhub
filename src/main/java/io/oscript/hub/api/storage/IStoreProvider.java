package io.oscript.hub.api.storage;

import java.io.IOException;
import java.util.List;

public interface IStoreProvider {
    //region Channels
    List<ChannelInfo> getChannels() throws Exception;

    ChannelInfo channelRegistration(String name) throws IOException;

    ChannelInfo getChannel(String name) throws IOException;

    void saveChannel(ChannelInfo channel) throws IOException;

    boolean existChannel(String name);

    //endregion

    //region Packages

    List<StoredPackageInfo> getPackages(String channel) throws Exception;

    StoredPackageInfo getPackage(String channel, String name) throws IOException;

    void savePackage(String channel, StoredPackageInfo pack);

    boolean existPackage(String channel, String name);

    //endregion

    // region Versions

    StoredVersionInfo getVersion(String channel, String name, String version) throws IOException;

    byte[] getPackageData(String channel, String name, String version) throws IOException;

    List<StoredVersionInfo> getVersions(String channel, String name) throws Exception;

    boolean saveVersionBin(SavingPackage pack);

    boolean saveVersion(SavingPackage pack);

    boolean saveVersion(String channel, StoredVersionInfo version);

    boolean existVersion(String channel, String name, String version);

    // endregion

}

