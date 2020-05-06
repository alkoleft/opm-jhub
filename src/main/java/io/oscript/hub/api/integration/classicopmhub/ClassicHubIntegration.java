package io.oscript.hub.api.integration.classicopmhub;

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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ClassicHubIntegration implements PackagesSource {

    static final Logger logger = LoggerFactory.getLogger(ClassicHubIntegration.class);

    @Autowired
    JSONSettingsProvider settings;

    @Autowired
    Storage store;

    Channel mainChannel;

    ClassicHubConfiguration config = new ClassicHubConfiguration();

    private final List<Package> packages = new ArrayList<>();
    private final List<Version> versions = new ArrayList<>();


    @PostConstruct
    void initialize() throws IOException {
        String description = "Загрузка настроек интеграции с opm hub";
        logger.info(description);

        try {
            config = settings.getConfiguration("opm-hub-mirror", ClassicHubConfiguration.class);
            logger.info(description, JSON.serialize(config));
        } catch (Exception e) {
            logger.error(String.format("Ошибка операции: %s", description), e);
        }

        if (config == null) {
            config = ClassicHubConfiguration.defaultConfiguration();
            settings.saveConfiguration("opm-hub-mirror", config);
        }
        mainChannel = store.registrationChannel(config.channel);
    }

    @Override
    public void sync() throws Exception {
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
                .filter(aPackage -> aPackage.versions.isEmpty())
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
        SavingPackage savingPackage = null;

        for (String server : config.getServers()) {
            savingPackage = downloadVersion(
                    MessageFormat.format("{0}/download/{1}/{1}-{2}.ospx", server, version.packageID, version.version),
                    String.format("%s/package/%s", server, version.packageID),
                    String.format("Загрузка версии пакета %s с %s", version.fullName(), server)
            );

            if (savingPackage != null)
                break;
        }

        return savingPackage;
    }

    Stream<SavingPackage> downloadLastVersions(Package pack) {
        List<SavingPackage> savingPackages = new ArrayList<>();

        for (String server : config.getServers()) {
            SavingPackage savingPackage = downloadVersion(
                    MessageFormat.format("{0}}/download/{1}/{1}.ospx", server, pack.name),
                    String.format("%s/package/%s", server, pack.name),
                    String.format("Загрузка актуальной версии пакета %s с %s", pack.getName(), server)
            );

            if (savingPackage != null) {
                savingPackages.add(savingPackage);
            }
        }

        return savingPackages.stream();
    }

    SavingPackage downloadVersion(String downloadURL, String packageURL, String description) {
        byte[] data;
        try {
            data = HttpRequest.download(URI.create(downloadURL));
            if (data != null) {
                VersionSourceInfo sourceInfo = new VersionSourceInfo();

                sourceInfo.setType(VersionSourceType.OPM_HUB);
                sourceInfo.setPackageURL(packageURL);
                sourceInfo.setVersionURL(downloadURL);

                var packageData = OspxPackage.parse(data);

                if (packageData == null) {
                    logger.error("Не удалось получить метаданные пакета");
                    return null;
                } else {
                    return new SavingPackage(packageData, PackageType.STABLE, sourceInfo, config.channel);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Ошибка операции %s", description), e);
        }

        return null;
    }

    Stream<String> loadPackageIDs(String server) {
        String description = "Получение списка пакетов";

        var stream = HttpRequest.request(URI.create(String.format("%s/download/list.txt", server)), description, HttpResponse.BodyHandlers.ofLines());
        if (stream == null) {
            return Stream.empty();
        }

        return stream;
    }

    List<Version> loadVersion(Package pack) {
        List<Version> packageVersions = new ArrayList<>();
        Set<String> versionKeys = new HashSet<>();

        for (String server : config.getServers()) {
            try {
                Stream.concat(versions(pack, server).stream(), versionsFromDownload(pack, server).stream())
                        .forEach(item -> {
                            if (!versionKeys.contains(item)) {
                                versionKeys.add(item);
                                packageVersions.add(new Version(item, server, pack.getName()));
                            }
                        });

            } catch (Exception e) {
                logger.error("Ошибка получения списка версий с хаба", e);
            }
        }

        return packageVersions;
    }

    static List<String> versions(Package pack, String serverURL) {

        return parseVersions(
                String.format("%s/package/%s", serverURL, pack.name),
                Pattern.compile("<a.+download/(.+)/\\1-(.+)\\.ospx\">\\2</a>"),
                pack.name);
    }

    static List<String> versionsFromDownload(Package pack, String serverURL) {

        return parseVersions(
                String.format("%s/download/%s", serverURL, pack.name),
                Pattern.compile(">(.+)-(.+)\\.ospx</a>"),
                pack.name);
    }

    static List<String> parseVersions(String url, Pattern pattern, String packageName) {
        logger.debug("Получение списка версий {}", url);

        List<String> versions = new ArrayList<>();

        String response = HttpRequest.request(url, "Получение списка версий (download/) с хаба");

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
