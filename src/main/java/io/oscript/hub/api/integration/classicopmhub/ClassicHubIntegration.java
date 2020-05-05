package io.oscript.hub.api.integration.classicopmhub;

import io.oscript.hub.api.controllers.PackagesController;
import io.oscript.hub.api.integration.PackageType;
import io.oscript.hub.api.integration.PackagesSource;
import io.oscript.hub.api.integration.VersionSourceInfo;
import io.oscript.hub.api.integration.VersionSourceType;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.storage.Channel;
import io.oscript.hub.api.storage.JSONSettingsProvider;
import io.oscript.hub.api.storage.SavingPackage;
import io.oscript.hub.api.storage.Storage;
import io.oscript.hub.api.utils.HttpRequest;
import io.oscript.hub.api.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ClassicHubIntegration implements PackagesSource {

    static final Logger logger = LoggerFactory.getLogger(PackagesController.class);

    @Autowired
    JSONSettingsProvider settings;

    @Autowired
    Storage store;

    Channel mainChannel;

    ClassicHubConfiguration config = new ClassicHubConfiguration();

    private List<Package> packages = new ArrayList<>();
    private List<Version> versions = new ArrayList<>();


    @PostConstruct
    void initialize() throws IOException {
        String description = "Загрузка настроек интеграции с opm hub";
        logger.info(description);

        try {
            config = settings.getConfiguration("opm-hub-mirror", ClassicHubConfiguration.class);
            logger.info(description, JSON.serialize(config));
        } catch (Exception e) {
            logger.error("Ошибка операции: " + description, e);
        }

        if (config == null) {
            config = ClassicHubConfiguration.defaultConfiguration();
            settings.saveConfiguration("opm-hub-mirror", config);
        }
        mainChannel = store.registrationChannel(config.channel);
    }

    @Override
    public void sync() {
        logger.info("Получение списка зарегистрированных пакетов");
        var packagesStream = config.getServers()
                .stream()
                .map(this::loadPackageIDs)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(Collectors.toSet());

        packages.clear();
        packagesStream.forEach(s -> packages.add(new Package(s)));

        logger.info("Получение списка версий пакетов");
        packages.forEach(aPackage -> {
            var loadedVersions = loadVersion(aPackage);
            versions.addAll(loadedVersions);
            aPackage.versions.addAll(loadedVersions);
        });

        if (!settings.saveConfiguration("opm-hub-packages", packages)) {
            logger.error("Не удалось сохранить список пакетов opm-hub");
        }

        logger.info("Загрузка новых версий пакетов");
        downloadPackages();
    }

    public void downloadPackages() {
        logger.info("Загрузка версий пакетов");

        getVersions().stream()
                .filter(version -> !mainChannel.containsVersion(version.packageID, version.version))
                .map(this::downloadVersion)
                .filter(Objects::nonNull)
                .forEach(mainChannel::pushPackage);
        logger.info("Загрузка пакетов, которые не содержат версий");
        packages.stream()
                .filter(aPackage -> aPackage.versions.size() == 0)
                .map(this::downloadLastVersions)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .filter(Objects::nonNull)
                .filter(savingPackage -> !mainChannel.containsVersion(savingPackage.getName(), savingPackage.getVersion()))
                .forEach(mainChannel::pushPackage);

    }

    public List<Version> getVersions() {
        return versions;
    }

    SavingPackage downloadVersion(Version version) {
        byte[] data;
        for (String server : config.getServers()) {
            try {
                URI downloadURL = downloadURL(server, version);
                data = HttpRequest.download(downloadURL);
                if (data != null) {
                    VersionSourceInfo sourceInfo = new VersionSourceInfo();

                    sourceInfo.setType(VersionSourceType.OPM_HUB);
                    sourceInfo.setPackageURL(packageURL(server, version).toString());
                    sourceInfo.setVersionURL(downloadURL.toString());

                    return new SavingPackage(OspxPackage.parse(data), PackageType.STABLE, sourceInfo, config.channel);
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    Stream<SavingPackage> downloadLastVersions(Package pack) {
        byte[] data;
        List<SavingPackage> packages = new ArrayList<>();
        for (String server : config.getServers()) {
            try {
                URI downloadURL = downloadURL(server, pack);
                data = HttpRequest.download(downloadURL);
                if (data != null) {
                    VersionSourceInfo sourceInfo = new VersionSourceInfo();

                    sourceInfo.setType(VersionSourceType.OPM_HUB);
                    sourceInfo.setPackageURL(packageURL(server, pack).toString());
                    sourceInfo.setVersionURL(downloadURL.toString());

                    packages.add(new SavingPackage(OspxPackage.parse(data), PackageType.STABLE, sourceInfo, config.channel));
                }
            } catch (Exception ignored) {
            }
        }

        return packages.stream();
    }

    URI downloadURL(String server, Version version) {
        return URI.create(String.format("%s/download/%s/%s-%s.ospx", server, version.packageID, version.packageID, version.version));
    }

    URI downloadURL(String server, Package pack) {
        return URI.create(String.format("%s/download/%s/%s.ospx", server, pack.name, pack.name));
    }

    URI packageURL(String server, Version version) {
        return URI.create(String.format("%s/package/%s", server, version.packageID));
    }

    URI packageURL(String server, Package pack) {
        return URI.create(String.format("%s/package/%s", server, pack.name));
    }

    Stream<String> loadPackageIDs(String server) {
        try {
            String description = "Получение списка пакетов";

            var stream = HttpRequest.request(URI.create(String.format("%s/download/list.txt", server)), description, HttpResponse.BodyHandlers.ofLines());
            if (stream == null) {
                return Stream.empty();
            }

            return stream;

        } catch (Exception e) {
            logger.error(String.format("Ошибка получения списка пакетов с %s", server), e);
            return Stream.empty();
        }
    }

    List<Version> loadVersion(Package pack) {
        List<Version> versions = new ArrayList<>();
        Set<String> versionKeys = new HashSet<>();

        for (String server : config.getServers()) {
            try {
                Stream.concat(versions(pack, server).stream(), versionsFromDownload(pack, server).stream())
                        .forEach(item -> {
                            if (!versionKeys.contains(item)) {
                                versionKeys.add(item);
                                versions.add(new Version(item, server, pack.getName()));
                            }
                        });

            } catch (Exception e) {
                logger.error("Ошибка получения списка версий с хаба", e);
            }
        }

        return versions;
    }

    static List<String> versions(Package pack, String serverURL) throws IOException, InterruptedException {

        return parseVersions(
                String.format("%s/package/%s", serverURL, pack.name),
                Pattern.compile("<a.+download\\/(.+)\\/\\1-(.+)\\.ospx\">\\2<\\/a>"),
                pack.name);
    }

    static List<String> versionsFromDownload(Package pack, String serverURL) throws IOException, InterruptedException {

        return parseVersions(
                String.format("%s/download/%s", serverURL, pack.name),
                Pattern.compile(">(.+)-(.+)\\.ospx<\\/a>"),
                pack.name);
    }

    static List<String> parseVersions(String url, Pattern pattern, String packageName) {
        logger.debug("Получение списка версий {}", url);

        List<String> versions = new ArrayList<>();

        String response = null;
        try {
            response = HttpRequest.request(url, "Получение списка версий (download/) с хаба");
        } catch (Exception ignored) {
        }

        if (response == null) {
            logger.error("Не удалось получить список версий {}", url);
            return versions;
        }

        var matcher = pattern.matcher(response);

        while (matcher.find()) {
            String packName = matcher.group(1);
            String version = matcher.group(2);

            if (!packageName.equalsIgnoreCase(packName)) {
                logger.error("Обработка {}. Ссылка на версию имеет другое имя пакета. {}@{}", packageName, packName, version);
                continue;
            }
            versions.add(version);
        }

        return versions;
    }

}
