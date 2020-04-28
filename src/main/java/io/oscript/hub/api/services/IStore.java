package io.oscript.hub.api.services;

import io.oscript.hub.api.data.IPackageMetadata;
import io.oscript.hub.api.data.Package;
import io.oscript.hub.api.data.PackageInfo;
import io.oscript.hub.api.ospx.OspxPackage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface IStore {
    List<Package> getPackages(String channel) throws IOException;

    Package getPackage(String packageName, String channel);

    PackageInfo getVersion(String packageName, String version, String channel);

    boolean savePackage(OspxPackage ospxPackage, String channel) throws IOException;

    byte[] getPackageData(IPackageMetadata metadata, String channel) throws IOException;
}
