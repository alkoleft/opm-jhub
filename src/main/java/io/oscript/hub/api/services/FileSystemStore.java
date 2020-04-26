package io.oscript.hub.api.services;

import io.oscript.hub.api.data.IPackageMetadata;
import io.oscript.hub.api.data.PackageInfo;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.Constants;
import io.oscript.hub.api.utils.JSON;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileSystemStore implements IStore {

    static final Path basePath = Path.of("c:\\tmp\\opm_hub\\");

    @Override
    public List<PackageInfo> getPackages(String channel) {

        Path path = getChannelPath(channel);

        try {
            return Files.list(path)
                    .filter(Files::isDirectory)
                    .map(item -> item.resolve(Constants.metadataFile))
                    .map(item -> JSON.deserialize(item, PackageInfo.class))
                    .sorted((item1, item2) -> item1.getName().compareToIgnoreCase(item2.getName()))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public PackageInfo getPackage(String packageName, String channel) {
        for (var packageInfo : getPackages(channel)) {
            if (packageInfo.getName().equals(packageName))
                return packageInfo;
        }
        return null;
    }

    @Override
    public PackageInfo getVersion(String packageName, String version, String channel) {
        Path path = getVersionPath(packageName, version, channel).resolve(Constants.metadataFile);

        return JSON.deserialize(path, PackageInfo.class);
    }

    public List<PackageInfo> getVersions(String packageName, String channel) {
        Path path = getPackagePath(packageName, channel);
        try {
            return Files.list(path)
                    .filter(item -> Files.isDirectory(path)
                            && !Files.isSymbolicLink(path)
                            && Files.exists(item.resolve(Constants.metadataFile)))
                    .sorted()
                    .map(item -> JSON.deserialize(item.resolve(Constants.metadataFile), PackageInfo.class))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean savePackage(OspxPackage ospxPackage, String channel) throws IOException {

        IPackageMetadata metadata = ospxPackage.getMetadata();

        String maxVersion = getVersions(metadata.getName(), channel).stream()
                .map(PackageInfo::getVersion)
                .max(String::compareToIgnoreCase)
                .orElse("");

        Path workPath = getVersionPath(metadata, channel);
        Files.createDirectories(workPath);

        JSON.serialize(metadata, workPath.resolve(Constants.metadataFile));

        FileOutputStream out = new FileOutputStream(workPath.resolve(Common.packageFileName(metadata)).toFile());
        ospxPackage.getPackageRaw().transferTo(out);
        out.close();

        if (maxVersion.compareToIgnoreCase(metadata.getVersion()) <= 0) {
            saveMaxVersion(ospxPackage, channel);
        }

        return true;
    }

    @Override
    public byte[] getPackageData(IPackageMetadata metadata, String channel) throws IOException {
        Path path = getVersionPath(metadata, channel);
        Path file = path.resolve(Common.packageFileName(metadata));

        return Files.readAllBytes(file);
    }

    protected Path getWorkPath() {
        return basePath;
    }

    protected Path getChannelPath(String channel) {
        return getWorkPath()
                .resolve(channel);
    }

    protected Path getPackagePath(PackageInfo packageInfo, String channel) {
        return getPackagePath(packageInfo.getName(), channel);
    }

    protected Path getPackagePath(String packageName, String channel) {
        return getWorkPath()
                .resolve(channel)
                .resolve(packageName);
    }

    protected Path getVersionPath(IPackageMetadata packageInfo, String channel) {
        return getVersionPath(packageInfo.getName(), packageInfo.getVersion(), channel);
    }

    protected Path getVersionPath(String packageName, String version, String channel) {
        return getWorkPath()
                .resolve(channel)
                .resolve(packageName)
                .resolve(version);
    }

    protected void saveMaxVersion(OspxPackage ospxPackage, String channel) throws IOException {
        Path path = getPackagePath(ospxPackage.getMetadata().getName(), channel);

        Path latestPath = path.resolve("latest");
        Path versionPath = getVersionPath(ospxPackage.getMetadata(), channel);
        Files.deleteIfExists(latestPath);

        Files.createSymbolicLink(latestPath, versionPath);
    }
}
