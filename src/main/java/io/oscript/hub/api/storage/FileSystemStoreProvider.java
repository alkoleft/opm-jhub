package io.oscript.hub.api.storage;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.data.IPackageMetadata;
import io.oscript.hub.api.utils.Common;
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

public class FileSystemStoreProvider implements IStoreProvider {

    static final Logger logger = LoggerFactory.getLogger(FileSystemStoreProvider.class);

    static String packageJSON = "package.json";
    static String versionJSON = "version.json";

    @Autowired
    public HubConfiguration configuration;

    List<ChannelInfo> channels;

    @PostConstruct
    public void init() {
        loadStoredChannels();
    }

    // region Channels
    @Override
    public List<ChannelInfo> getChannels() {
        return channels;
    }

    @Override
    public ChannelInfo getChannel(String name) {
        return channels.stream()
                .filter(channel -> channel.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ChannelInfo channelRegistration(String name) {
        return channelRegistration(name, false);
    }

    public ChannelInfo channelRegistration(String name, boolean isDefault) {
        ChannelInfo result;
        if (null == (result = getChannel(name))) {
            channels.add(result = new ChannelInfo(name, isDefault));
            saveChannel(result);
        }
        return result;
    }

    @Override
    public boolean saveChannel(ChannelInfo channel) {
        try {
            JSON.serialize(channels, pathStoredChannels());
            return true;
        } catch (IOException e) {
            logger.error("Ошибка сохранения списка каналов", e);
            return false;
        }
    }

    // endregion Channels

    // region Packages

    @Override
    public List<StoredPackageInfo> getPackages(String channelName) throws IOException {
        Path channelPath = getChannelPath(channelName);

        createPath(channelPath);

        return Files.list(channelPath)
                .filter(path -> pathFilter(path, packageJSON))
                .map(path -> JSON.deserialize(path.resolve(packageJSON), StoredPackageInfo.class))
                .collect(Collectors.toList());
    }

    @Override
    public StoredPackageInfo getPackage(String channel, String packageName) {
        Path path = getPackagePath(packageName, channel);

        if (!pathFilter(path, packageJSON)) {
            return null;
        }

        return JSON.deserialize(path.resolve(packageJSON), StoredPackageInfo.class);
    }

    // endregion Packages

    //region Versions

    @Override
    public List<StoredVersionInfo> getVersions(String channel, String name) throws IOException {
        Path packagePath = getPackagePath(name, channel);
        return Files.list(packagePath)
                .filter(path -> pathFilter(path, versionJSON))
                .map(path -> JSON.deserialize(path.resolve(versionJSON), StoredVersionInfo.class))
                .collect(Collectors.toList());
    }

    @Override
    public StoredVersionInfo getVersion(String channel, String name, String version) {
        Path path = getVersionPath(name, version, channel);

        if (!pathFilter(path, versionJSON))
            return null;

        return JSON.deserialize(path.resolve(versionJSON), StoredVersionInfo.class);
    }

    //endregion

    @Override
    public byte[] getPackageData(String channel, String name, String version) throws IOException {
        Path path = getVersionPath(name, version, channel);
        Path file = path.resolve(Common.packageFileName(name, version));

        if (Files.exists(file)) {
            return Files.readAllBytes(file);
        } else {
            return null;
        }
    }

    // region save

    @Override
    public boolean saveVersion(SavingPackage pack) {
        return saveVersion(pack.channel, StoredVersionInfo.create(pack));
    }

    @Override
    public boolean saveVersion(String channel, StoredVersionInfo storedVersion) {
        Path versionPath = getVersionPath(storedVersion.getMetadata(), channel);

        try {
            Files.createDirectories(versionPath);
            JSON.serialize(storedVersion, versionPath.resolve(versionJSON));
            return true;
        } catch (IOException e) {
            logger.error("Ошибка сохранения информации о версии", e);
            return false;
        }
    }

    @Override
    public boolean existVersion(String channel, String name, String version) {
        Path path = getVersionPath(name, version, channel);

        return pathFilter(path, versionJSON) && Files.exists(path.resolve(Common.packageFileName(name, version)));
    }

    @Override
    public boolean savePackage(String channel, StoredPackageInfo pack) {
        Path packagePath = getPackagePath(pack.getName(), channel);
        createPath(packagePath);

        try {
            JSON.serialize(pack, packagePath.resolve(packageJSON));
            return true;
        } catch (IOException e) {
            logger.error("Ошибка сохранения метаданных пакета", e);
            return false;
        }
    }

    @Override
    public boolean existPackage(String channel, String name) {
        Path path = getPackagePath(name, channel);

        return pathFilter(path, packageJSON);
    }

    public boolean saveVersionBin(SavingPackage pack) {
        Path versionPath = getVersionPath(pack.getName(), pack.getVersion(), pack.getChannel());
        createPath(versionPath);

        Path versionBin = versionPath.resolve(Common.packageFileName(pack.packageData.getMetadata()));
        try {
            FileOutputStream out = new FileOutputStream(versionBin.toFile());
            pack.packageData.getPackageRaw().transferTo(out);
            return true;
        } catch (Exception e) {
            logger.error("Ошибка записи " + versionBin.toString(), e);
            return false;
        }
    }
    
    public void loadStoredChannels() {
        channels = JSON.deserializeList(pathStoredChannels(), ChannelInfo.class);

        if (channels == null) {
            channels = new ArrayList<>();
            channelRegistration("stable", true);
        }
    }

    // endregion

    boolean pathFilter(Path path, String metadataFile) {
        return Files.exists(path)
                && Files.isDirectory(path)
                && !Files.isSymbolicLink(path)
                && Files.exists(path.resolve(metadataFile));
    }

    // region paths

    protected Path getWorkPath() {
        return configuration.getStorePath();
    }

    protected Path pathStoredChannels() {
        return configuration.getSettingsPath().resolve("channels.json");
    }

    protected Path getChannelPath(String channel) {
        return createPath(getWorkPath().resolve(channel));
    }

    protected Path getPackagePath(String packageName, String channel) {
        return createPath(getChannelPath(channel).resolve(packageName));
    }

    protected Path getVersionPath(IPackageMetadata packageInfo, String channel) {
        return getVersionPath(packageInfo.getName(), packageInfo.getVersion(), channel);
    }

    protected Path getVersionPath(String packageName, String version, String channel) {
        var path = getWorkPath()
                .resolve(channel)
                .resolve(packageName)
                .resolve(version);

        return createPath(path);
    }

    protected Path createPath(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                logger.error("Ошибка создания каталога " + path.toAbsolutePath(), e);
            }
        }
        return path;
    }

    //endregion
}
