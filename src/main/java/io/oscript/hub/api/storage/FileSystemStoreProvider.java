package io.oscript.hub.api.storage;

import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileSystemStoreProvider implements IStoreProvider {

    static final Logger logger = LoggerFactory.getLogger(FileSystemStoreProvider.class);

    private static final String CHANNEL_JSON = "channel.json";
    private static final String PACKAGE_JSON = "package.json";
    private static final String VERSION_JSON = "version.json";

    @Value("${hub.workpath}")
    private Path workPath;

    @PostConstruct
    public void init() {
        Common.createPath(getWorkPath());
    }

    // region Channels
    @Override
    public List<ChannelInfo> getChannels() throws Exception {
        return loadMetadata(getWorkPath(), CHANNEL_JSON, ChannelInfo.class);
    }

    @Override
    public ChannelInfo getChannel(String name) throws IOException {
        Path path = getChannelPath(name);

        if (!pathFilter(path, CHANNEL_JSON))
            return null;

        return JSON.deserialize(path.resolve(CHANNEL_JSON), ChannelInfo.class);
    }

    @Override
    public ChannelInfo channelRegistration(String name) throws IOException {
        ChannelInfo channelInfo;
        if (null == (channelInfo = getChannel(name))) {
            channelInfo = new ChannelInfo(name);
            saveChannel(channelInfo);
        }
        return channelInfo;
    }


    @Override
    public void saveChannel(ChannelInfo channel) throws IOException {
        Path channelPath = getChannelPath(channel.name);
        Common.createPath(channelPath);
        JSON.serialize(channel, channelPath.resolve(CHANNEL_JSON));
    }

    @Override
    public boolean existChannel(String name) {
        Path path = getChannelPath(name);

        return pathFilter(path, CHANNEL_JSON);
    }

    // endregion Channels

    // region Packages

    @Override
    public List<StoredPackageInfo> getPackages(String channel) throws Exception {
        if (!existChannel(channel)) {
            return List.of();
        }

        Path channelPath = getChannelPath(channel);

        return loadMetadata(channelPath, PACKAGE_JSON, StoredPackageInfo.class);
    }

    @Override
    public StoredPackageInfo getPackage(String channel, String name) throws IOException {
        if (!existPackage(channel, name)) {
            return null;
        }

        Path path = getPackagePath(name, channel);

        return JSON.deserialize(path.resolve(PACKAGE_JSON), StoredPackageInfo.class);
    }

    @Override
    public boolean existPackage(String channel, String name) {
        Path path = getPackagePath(name, channel);

        return pathFilter(path, PACKAGE_JSON);
    }

    @Override
    public void savePackage(String channel, StoredPackageInfo pack) {
        Path packagePath = getPackagePath(pack.getName(), channel);
        Common.createPath(packagePath);

        try {
            JSON.serialize(pack, packagePath.resolve(PACKAGE_JSON));
        } catch (IOException e) {
            logger.error("Ошибка сохранения метаданных пакета", e);
        }
    }

    // endregion Packages

    //region Versions

    @Override
    public List<StoredVersionInfo> getVersions(String channel, String name) throws Exception {

        if (!existPackage(channel, name)) {
            return null;
        }

        Path packagePath = getPackagePath(name, channel);

        return loadMetadata(packagePath, VERSION_JSON, StoredVersionInfo.class);
    }

    @Override
    public StoredVersionInfo getVersion(String channel, String name, String version) throws IOException {
        if (!existVersion(channel, name, version)) {
            return null;
        }

        Path path = getVersionPath(name, version, channel);

        return JSON.deserialize(path.resolve(VERSION_JSON), StoredVersionInfo.class);
    }

    @Override
    public boolean existVersion(String channel, String name, String version) {
        Path path = getVersionPath(name, version, channel);

        return pathFilter(path, VERSION_JSON) && Files.exists(path.resolve(Common.packageFileName(name, version)));
    }

    @Override
    public byte[] getPackageData(String channel, String name, String version) throws IOException {
        if (!existVersion(channel, name, version)) {
            return null;
        }

        Path path = getVersionPath(name, version, channel);
        String packageFileName = Common.packageFileName(name, version);

        return Files.readAllBytes(path.resolve(packageFileName));
    }

    @Override
    public boolean saveVersion(SavingPackage pack) {
        return saveVersion(pack.getChannel(), StoredVersionInfo.create(pack));
    }

    @Override
    public boolean saveVersion(String channel, StoredVersionInfo storedVersion) {
        Path versionPath = getVersionPath(storedVersion.getMetadata(), channel);

        Common.createPath(versionPath);
        try {
            JSON.serialize(storedVersion, versionPath.resolve(VERSION_JSON));
            return true;
        } catch (IOException e) {
            logger.error("Ошибка сохранения информации о версии", e);
            return false;
        }
    }

    public boolean saveVersionBin(SavingPackage pack) {
        Path versionPath = getVersionPath(pack.getName(), pack.getVersion(), pack.getChannel());
        Common.createPath(versionPath);

        Path versionBin = versionPath.resolve(Common.packageFileName(pack.getPackageData().getMetadata()));
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(versionBin.toFile());
            pack.getPackageData().getPackageRaw().transferTo(out);
            return true;
        } catch (Exception e) {
            logger.error(String.format("Ошибка записи %s", versionBin), e);
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error(String.format("Закрытия потока записи в %s", versionBin), e);
                }
            }
        }
    }

    // endregion

    <T> List<T> loadMetadata(Path itemsPath, String metadataFile, Class<T> type) throws Exception {
        Map<Path, Exception> exceptions = new LinkedHashMap<>();
        List<T> items;
        try (var list = Files.list(itemsPath)) {
            items = list
                    .filter(path -> pathFilter(path, metadataFile))
                    .map(path -> {
                        try {
                            return JSON.deserialize(path.resolve(metadataFile), type);
                        } catch (IOException e) {
                            exceptions.put(path, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        exceptions.forEach((path, e) -> logger.error(String.format("Ошибка чтения %s", path), e));

        if (items.isEmpty() && !exceptions.isEmpty()) {
            throw new Exception("Не удалось загрузить " + type.getSimpleName());
        }
        return items;
    }

    // region paths

    protected Path getWorkPath() {
        return workPath.resolve("store");
    }

    protected Path getChannelPath(String channel) {
        return getWorkPath().resolve(channel);
    }

    protected Path getPackagePath(String packageName, String channel) {
        return getChannelPath(channel).resolve(packageName);
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

    boolean pathFilter(Path path, String metadataFile) {
        return Files.exists(path)
                && Files.isDirectory(path)
                && !Files.isSymbolicLink(path)
                && Files.exists(path.resolve(metadataFile));
    }

    //endregion
}
