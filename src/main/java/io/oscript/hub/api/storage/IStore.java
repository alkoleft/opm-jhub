package io.oscript.hub.api.storage;

import io.oscript.hub.api.data.IPackageMetadata;
import io.oscript.hub.api.data.PackageInfo;
import io.oscript.hub.api.ospx.Metadata;
import io.oscript.hub.api.ospx.OspxPackage;

import java.io.IOException;
import java.util.List;

public interface IStore {
    List<StoredPackageInfo> getPackages(String channel) throws IOException;

    StoredPackageInfo getPackage(String packageName, String channel);

    StoredVersionInfo getVersion(String packageName, String version, String channel);

    boolean savePackage(OspxPackage ospxPackage, String channel) throws IOException;

    boolean savePackage(SavingPackage pack);

    byte[] getPackageData(IPackageMetadata metadata, String channel) throws IOException;

    boolean containsVersion(String packageID, String version, String channel);

    boolean containsVersion(String packageID, String version);
}

