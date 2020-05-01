package io.oscript.hub.api.integration.classicopmhub;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.controllers.ListController;
import io.oscript.hub.api.integration.PackageBase;
import io.oscript.hub.api.integration.PackagesSource;
import io.oscript.hub.api.utils.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ClassicHubIntegration implements PackagesSource {

    static final Logger logger = LoggerFactory.getLogger(ListController.class);

    ClassicHubConfiguration configuration = new ClassicHubConfiguration();

    private List<Package> packages = new ArrayList<>();
    private List<Version> versions = new ArrayList<>();

    @Autowired
    HubConfiguration appConfiguration;

    @Override
    public void sync() {
        var packagesStream = configuration.getServers()
                .stream()
                .map(this::loadPackageIDs)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(Collectors.toSet());

        packages.clear();
        packagesStream.forEach(s -> packages.add(new Package(s)));
        packages.forEach(aPackage -> {
            var loadedVersions = loadVersion(aPackage);
            versions.addAll(loadedVersions);
            aPackage.versions.addAll(loadedVersions);
        });

        if (!appConfiguration.saveConfiguration("opm-hub-packages", packages)) {
            logger.error("Не удалось сохранить список пакетов opm-hub");
        }
    }

    @Override
    public PackageBase[] getPackages() {
        return packages.toArray(PackageBase[]::new);
    }

    public List<Version> getVersions() {
        return versions;
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

        for (String server : configuration.getServers()) {
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
