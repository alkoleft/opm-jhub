package io.oscript.hub.api.services;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.data.IPackageMetadata;
import io.oscript.hub.api.data.Package;
import io.oscript.hub.api.data.PackageInfo;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.Constants;
import io.oscript.hub.api.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileSystemStore implements IStore {

    static final Logger logger = LoggerFactory.getLogger(FileSystemStore.class);

    @Autowired
    public HubConfiguration configuration;

    List<Package> packages;

    @Override
    public List<Package> getPackages(String channel) {
        return packages;
    }

    @PostConstruct
    public void init() {
        getAllPackages();
    }

    public Package getPackage(String packageName) {
        if (Common.isNullOrEmpty(packageName))
            return null;
        return packages.stream().filter(item -> item.getName().equalsIgnoreCase(packageName)).findFirst().orElse(null);
    }

    @Override
    public Package getPackage(String packageName, String channel) {
        return getPackage(packageName);
    }

    @Override
    public PackageInfo getVersion(String packageName, String version, String channel) {
        if (Common.isNullOrEmpty(version) || version.equalsIgnoreCase("latest")) {
            version = getPackage(packageName, channel).getVersion();
        }
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

        updatePackage(ospxPackage);

        return true;
    }

    void updatePackage(OspxPackage ospxPackage) {
        var metadata = ospxPackage.getMetadata();
        Package pack = getAllPackages().stream().filter((Package item) -> item.getName().equalsIgnoreCase(metadata.getName())).findFirst().orElse(null);

        if (pack == null) {
            pack = new Package();
            pack.setName(metadata.getName());
            pack.setDescription(metadata.getDescription());
            pack.setVersion(metadata.getVersion());
            packages.add(pack);
        } else if (pack.getVersion() == null || pack.getVersion().compareToIgnoreCase(metadata.getVersion()) <= 0) {
            pack.setVersion(metadata.getVersion());
        }

        Path path = getPackagesInfoPath().resolve(String.format("%s.json", pack.getName()));
        try {
            JSON.serialize(pack, path);
        } catch (Exception ignore) {
        }
    }

    @Override
    public byte[] getPackageData(IPackageMetadata metadata, String channel) throws IOException {
        Path path = getVersionPath(metadata, channel);
        Path file = path.resolve(Common.packageFileName(metadata));

        return Files.readAllBytes(file);
    }

    protected Path getWorkPath() {
        return configuration.getStorePath();
    }

    protected Path getPackagesInfoPath() {
        try {
            Path path = configuration.getSettingsPath().resolve("packages");
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }
            return path;
        } catch (Exception ignore) {
        }

        return null;
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
//        Path path = getPackagePath(ospxPackage.getMetadata().getName(), channel);
//
//        Path latestPath = path.resolve("latest");
//        Path versionPath = getVersionPath(ospxPackage.getMetadata(), channel);
//        Files.deleteIfExists(latestPath);
//
//        Files.createSymbolicLink(latestPath, versionPath);
    }

    public List<Package> getAllPackages() {
        if (packages == null) {
            packages = new ArrayList<>();
            try {
                Files.list(getPackagesInfoPath()).forEach(file -> {
                            packages.add(JSON.deserialize(file, Package.class));
                        }
                );

            } catch (Exception e) {
                logger.error("Ошибка загрузки списка пакетов", e);
            }
        }

        for (var pack : packages) {
            getVersions(pack.getName(), Constants.defaultChannel).forEach(item -> {
                pack.getVersions().add(item.getVersion());
            });
        }
        return packages;
    }

    public boolean containsVersion(String packageID, String version) {
        return getVersion(packageID, version, Constants.defaultChannel) != null;
    }
}
