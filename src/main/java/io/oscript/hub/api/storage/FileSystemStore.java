package io.oscript.hub.api.storage;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.data.IPackageMetadata;
import io.oscript.hub.api.data.PackageInfo;
import io.oscript.hub.api.integration.VersionSourceInfo;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystemStore implements IStore {

    static final Logger logger = LoggerFactory.getLogger(FileSystemStore.class);

    @Autowired
    public HubConfiguration configuration;

    List<StoredPackageInfo> packages;
    List<Channel> channels;

    @PostConstruct
    public void init() {
        loadPackages();

        loadStoredChannels();
    }

    // region Channels
    @Override
    public List<Channel> getChannels() {
        return channels;
    }

    @Override
    public Channel getChannel(String name) {
        return channels.stream()
                .filter(channel -> channel.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Channel channelRegistration(String name) {
        return channelRegistration(new Channel(name, false));
    }

    @Override
    public Channel channelRegistration(Channel channel) {
        Channel result;
        if (null == (result = getChannel(channel.name))) {
            channels.add(result = channel);

            updateStoredChannels();

            collectChannelPackages(channel);
        }
        return result;
    }

    // endregion Channels

    // region Packages

    public List<StoredPackageInfo> getPackages() {
        return packages;
    }

    public StoredPackageInfo getPackage(String packageName) {
        if (Common.isNullOrEmpty(packageName))
            return null;
        return packages.stream().filter(item -> item.getName().equalsIgnoreCase(packageName)).findFirst().orElse(null);
    }

    @Override
    public List<StoredPackageInfo> getPackages(String channelName) {
        var channel = getChannel(channelName);
        if (channel == null)
            return null;
        return channel.packages;
    }

    @Override
    public StoredPackageInfo getPackage(String packageName, String channel) {
        return getPackage(packageName);
    }

    // endregion Packages

    //region Versions
    @Override
    public StoredVersionInfo getVersion(String packageName, String version, String channel) {
        if (Common.isNullOrEmpty(version) || version.equalsIgnoreCase("latest")) {
            version = getPackage(packageName, channel).getVersion();
        }
        Path path = getVersionPath(packageName, version, channel).resolve(Constants.metadataFile);

        if (Files.notExists(path))
            return null;
        return Objects.requireNonNull(JSON.deserialize(path, StoredVersionInfo.class));
    }

    @Override
    public List<StoredVersionInfo> getVersions(String packageName) {

        return loadVersions(packageName, Constants.defaultChannel);
    }

    @Override
    public List<StoredVersionInfo> getVersions(String packageName, String channel) {

        return loadVersions(packageName, channel);
    }

    protected List<StoredVersionInfo> loadVersions(String packageName, String channel) {
        Path path = getPackagePath(packageName, channel);
        try {
            return Files.list(path)
                    .filter(item -> Files.isDirectory(path)
                            && !Files.isSymbolicLink(path)
                            && Files.exists(item.resolve(Constants.metadataFile)))
                    .sorted()
                    .map(item -> JSON.deserialize(item.resolve(Constants.metadataFile), StoredVersionInfo.class))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    //endregion

    @Override
    public boolean savePackage(OspxPackage ospxPackage, String channel) {

        return savePackage(new SavingPackage(ospxPackage, VersionSourceInfo.UNKNOWN, channel));
    }

    @Override
    public boolean savePackage(SavingPackage pack) {

        return savePackageData(pack) && savePackageInfo(pack);
    }

    @Override
    public byte[] getPackageData(IPackageMetadata metadata, String channel) throws IOException {
        Path path = getVersionPath(metadata, channel);
        Path file = path.resolve(Common.packageFileName(metadata));

        return Files.readAllBytes(file);
    }

    public boolean containsVersion(String packageID, String version) {
        return containsVersion(packageID, version, Constants.defaultChannel);
    }

    public boolean containsVersion(String packageID, String version, String channel) {
        return getVersion(packageID, version, channel) != null;
    }

    // region save

    protected boolean savePackageData(SavingPackage pack) {
        IPackageMetadata metadata = pack.packageData.getMetadata();

        String prefix = String.format("Сохранение %s, канал %s. ", metadata.fullName(), pack.channel);
        logger.debug(prefix);

        Path workPath = getVersionPath(metadata, pack.channel);
        logger.debug("каталог хранения {}", workPath);

        try {
            Files.createDirectories(workPath);

            FileOutputStream out = new FileOutputStream(workPath.resolve(Common.packageFileName(metadata)).toFile());
            pack.packageData.getPackageRaw().transferTo(out);
            out.close();

            logger.info(prefix + "Успешно");

        } catch (Exception e) {
            logger.error(prefix + "Ошибка", e);
            return false;
        }

        return true;
    }

    protected boolean savePackageInfo(SavingPackage pack) {
        IPackageMetadata metadata = pack.packageData.getMetadata();

        String prefix = String.format("Сохранение метаданных %s, канал %s. ", metadata.fullName(), pack.channel);
        logger.debug(prefix);

        Path workPath = getVersionPath(metadata, pack.channel);
        logger.debug("каталог хранения {}", workPath);

        StoredVersionInfo storedVersion = new StoredVersionInfo();
        storedVersion.metadata = new Metadata();

        storedVersion.metadata.setName(pack.packageData.getMetadata().getName());
        storedVersion.metadata.setVersion(pack.packageData.getMetadata().getVersion());
        storedVersion.metadata.setEngineVersion(pack.packageData.getMetadata().getEngineVersion());
        storedVersion.metadata.setAuthor(pack.packageData.getMetadata().getAuthor());
        storedVersion.metadata.setAuthorEmail(pack.packageData.getMetadata().getAuthorEmail());
        storedVersion.metadata.setDescription(pack.packageData.getMetadata().getDescription());
        storedVersion.metadata.setDependencies(pack.packageData.getMetadata().getDependencies());

        storedVersion.source = pack.sourceInfo;
        storedVersion.saveData = new Date();
        try {
            Files.createDirectories(workPath);
            JSON.serialize(storedVersion, workPath.resolve(Constants.metadataFile));

            logger.debug("Успешно");
        } catch (Exception e) {
            logger.error(prefix + "Ошибка", e);
            return false;
        }

        updateStoredPackageInfo(storedVersion);

        return true;

    }

    void updateStoredPackageInfo(StoredVersionInfo pack) {
        logger.debug("Обновление информации о хранимых пакетах");

        var metadata = pack.getMetadata();
        StoredPackageInfo packInfo = packages.stream().filter((StoredPackageInfo item) -> item.getName().equalsIgnoreCase(metadata.getName())).findFirst().orElse(null);

        if (packInfo == null) {
            packInfo = new StoredPackageInfo();
            packInfo.setName(metadata.getName());
            packInfo.setDescription(metadata.getDescription());
            packInfo.setVersion(metadata.getVersion());
            packInfo.setMetadata(metadata);
            packages.add(packInfo);
        } else if (packInfo.getVersion() == null || packInfo.getVersion().compareToIgnoreCase(metadata.getVersion()) <= 0) {
            packInfo.setVersion(metadata.getVersion());
            packInfo.setMetadata(metadata);
        }

        Path path = getPackagesInfoPath().resolve(String.format("%s.json", packInfo.getName()));
        try {
            JSON.serialize(packInfo, path);
        } catch (Exception ignore) {
        }
    }

    public List<StoredPackageInfo> loadPackages() {
        if (packages == null) {
            packages = new ArrayList<>();
            try {
                Files.list(getPackagesInfoPath()).forEach(file -> {
                            packages.add(JSON.deserialize(file, StoredPackageInfo.class));
                        }
                );

            } catch (Exception e) {
                logger.error("Ошибка загрузки списка пакетов", e);
            }
        }

        for (var pack : packages) {
            loadVersions(pack.getName(), Constants.defaultChannel).forEach(item -> {
                pack.getVersions().add(item.getMetadata().getVersion());
            });
        }
        return packages;
    }

    public void loadStoredChannels() {
        channels = JSON.deserializeList(pathStoredChannels(), Channel.class);

        if (channels == null) {
            channels = new ArrayList<>();
            channelRegistration(new Channel("stable", true));
        }

        for (Channel channel : channels) {
            collectChannelPackages(channel);
        }
    }

    public boolean updateStoredChannels() {
        try {
            JSON.serialize(channels, pathStoredChannels());
            return true;
        } catch (IOException e) {
            logger.error("Ошибка сохранения информации о каналах", e);
            return false;
        }
    }

    // endregion

    void collectChannelPackages(Channel channel) {
        Path channelPath = getChannelPath(channel.name);

        Stream<Path> subPaths = null;
        try {
            subPaths = Files.list(channelPath).filter(Files::isDirectory);
        } catch (IOException e) {
            logger.error("Ошибка поиска пакетов канала " + channel.name, e);
            return;
        }
        subPaths.forEach(path -> {
            String packageName = path.getFileName().toString();
            var pack = getPackage(packageName);
            if (pack != null) {
                channel.packages.add(pack);
            }
        });
    }

    // region paths

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

    protected Path pathStoredChannels() {
        return configuration.getSettingsPath().resolve("channels.json");
    }

    protected Path getChannelPath(String channel) {
        return createPath(getWorkPath().resolve(channel));
    }

    protected Path getPackagePath(PackageInfo packageInfo, String channel) {
        return getPackagePath(packageInfo.getName(), channel);
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
