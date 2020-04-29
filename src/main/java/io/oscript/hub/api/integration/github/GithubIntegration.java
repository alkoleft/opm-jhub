package io.oscript.hub.api.integration.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.utils.JSON;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GithubIntegration {

    @Autowired
    static HubConfiguration configuration;

    final static Logger logger = LoggerFactory.getLogger(GithubIntegration.class);
    static GitHub client;
    static CollectRepositoriesConfig config;
    static List<Repository> repositories;

    @PostConstruct
    public static void init() throws IOException {
        var stream = configuration.getConfiguration("repositories");
        if (stream == null) {
            repositories = new ArrayList<>();
        } else {
            repositories = JSON.deserializeList(stream, Repository.class);
            stream.close();
            repositories.forEach(repository -> repository.getReleases().forEach(release -> release.repository = repository));
        }

        stream = configuration.getConfiguration("github");
        if (stream == null) {
            config = new CollectRepositoriesConfig();
        } else {
            config = JSON.deserialize(stream, CollectRepositoriesConfig.class);
            stream.close();
        }
    }

    public static void save() throws IOException {
        saveRepositories();
    }

    static void saveRepositories() throws IOException {
        configuration.saveConfiguration("repositories", repositories);
    }

    public static List<Repository> getRepositories() {
        return repositories;
    }

    public static List<Repository> loadRepositories(Path file) {
        return JSON.deserializeList(file, Repository.class);
    }

    static Stream<Repository> findNewRepositories(GHPerson person) {
        try {
            return person
                    .getRepositories()
                    .values()
                    .stream()
                    .filter(ghrep -> repositories.stream()
                            .filter(exists -> exists.fullName.equalsIgnoreCase(ghrep.getFullName()))
                            .findFirst()
                            .isEmpty()
                    )
                    .map(Repository::create)
                    .filter(Objects::nonNull);
        } catch (Exception e) {
            logger.error("Ошибка обработки репозитория", e);
            return null;
        }
    }

    public static void findNewRepositories() throws IOException {
        var newRepositories = Stream.concat(
                config.organizations.stream().map(item -> getSource(GithubSourceType.Organisation, item)),
                config.users.stream().map(item -> getSource(GithubSourceType.User, item))
        )
                .filter(Objects::nonNull)
                .map(GithubIntegration::findNewRepositories)
                .filter(Objects::nonNull)
                .reduce(Stream::concat);
        if (newRepositories.isPresent()) {
            newRepositories.get().forEach(repositories::add);
            saveRepositories();
        }
    }

    static boolean suitableRelease(GHRelease ghRelease, Release lastRelease) {
        if (ghRelease.isDraft() || ghRelease.isPrerelease())
            return false;

        return lastRelease == null || lastRelease.getVersion().compareTo(ghRelease.getTagName()) < 0;
    }

    static Release createRelease(GHRelease ghRelease) {
        Release release = new Release();
        release.setTag(ghRelease.getTagName());
        release.setZipUrl(ghRelease.getZipballUrl());
        try {
            release.setDate(ghRelease.getCreatedAt());
        } catch (IOException ignore) {
        }

        try {
            for (var asset : ghRelease.getAssets()) {
                if (asset.getName().endsWith(".ospx")) {
                    release.setPackageUrl(asset.getBrowserDownloadUrl());
                }

            }
        } catch (IOException ignore) {
        }
        return release;
    }

    static Stream<Release> loadReleases(Repository rep, boolean onlyNew) throws IOException {
        Release lastRelease = onlyNew ? rep.maxRelease() : null;
        GHRepository repository = getClient().getRepository(rep.getFullName());

        return StreamSupport.stream(repository.listReleases().spliterator(), false)
                .filter(item -> suitableRelease(item, lastRelease))
                .map(GithubIntegration::createRelease);

    }

    public static List<Repository> findNewReleases() throws IOException {

        List<Repository> repositoriesWithNewReleases = new ArrayList<>();

        for (Repository rep : repositories) {
            var newReleases = loadReleases(rep, true);
            newReleases.forEach(rep::addRelease);

            if (newReleases.findFirst().isPresent()) {
                repositoriesWithNewReleases.add(rep);
            }
        }

        return repositoriesWithNewReleases;
    }

    public static List<io.oscript.hub.api.integration.Package> collectInfo(GithubSource source) throws IOException, InterruptedException {
        var packages = packages(source);

        for (var pack : packages) {
            versions((Package) pack);
        }

        saveVersionsInfo(packages);

        return packages;
    }

    public static List<io.oscript.hub.api.integration.Package> packages(GithubSource source) throws IOException, InterruptedException {

        List<io.oscript.hub.api.integration.Package> packages = new ArrayList<>();

        var person = getSource(source);

        if (person == null) {
            return packages;
        }

        Map<String, GHRepository> repositories = person.getRepositories();

        for (GHRepository repository : repositories.values()) {
            Package aPackage = getPackage(repository, person);

            packages.add(aPackage);

            if (packages.size() >= 10)
                break;
        }

        return packages;
    }

    public static List<io.oscript.hub.api.integration.Version> versions(Package aPackage) throws IOException {

        logger.info("Получение информации о релизах {}", aPackage.getName());
        for (GHRelease release : aPackage.repository.listReleases()) {
            if (release.isDraft())
                continue;
            if (release.isPrerelease())
                continue;
            Version version = new Version(release);
            aPackage.getVersions().add(version);
        }

        return aPackage.getVersions();
    }

    public static byte[] download(URI url) throws IOException, InterruptedException {
        var request = java.net.http.HttpRequest.newBuilder()
                .GET()
                .uri(url)
                //.setHeader("Authorization", "token " + System.getProperty("github.token"))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            return null;
        }

        return response.body();
    }

    public static Package packageByID(GithubSource source, String repoID) throws IOException {
        var person = getSource(source);

        if (person == null) {
            return null;
        }

        var repository = person.getRepository(repoID);

        return getPackage(repository, person);
    }

    static GHPerson getSource(GithubSource source) throws IOException {
        return getSource(source.type, source.value);
    }

    static GitHub getClient() throws IOException {
        if (client == null) {
            String token = System.getProperty("github.token");
            client = GitHub.connectUsingOAuth(token);
        }
        return client;
    }

    static GHPerson getSource(GithubSourceType type, String value) {
        try {

            var client = getClient();
            switch (type) {
                case Organisation:
                    return client.getOrganization(value);
                case User:
                    return client.getUser(value);
                default:
                    logger.error("Неизвестный тип источника");
                    return null;
            }
        } catch (Exception e) {
            logger.error("Ошибка поиска группы репозиториев", e);
            return null;
        }

    }

    static Package getPackage(GHRepository repository, GHPerson source) throws IOException {
        repository = getMainRepository(repository);

        return new Package(repository);
    }

    static GHRepository getMainRepository(GHRepository repository) throws IOException {
        String fullName = repository.getFullName();
        if (repository.isFork()) {
            var client = getClient();

            if (repository.getSource() == null) {
                repository = client.getRepositoryById(String.valueOf(repository.getId())).getSource();
            } else {
                repository = repository.getSource();
            }
            logger.info("{} -> {}", fullName, repository.getFullName());
        }

        return repository;
    }

    static class Package extends io.oscript.hub.api.integration.Package {
        @JsonIgnore
        GHRepository repository;

        Package(GHRepository repository) {
            this.url = repository.getUrl().toString();
            this.name = repository.getName();
            this.repository = repository;
        }
    }

    static class Version extends io.oscript.hub.api.integration.Version {

        @JsonIgnore
        GHRelease release;

        public Version(GHRelease release) {
            super(release.getTagName(), release.getZipballUrl());
            if (name.startsWith("v"))
                name = name.substring(1);

            this.release = release;
            this.url = release.getUrl().toString();
        }

    }

    static void saveVersionsInfo(List<io.oscript.hub.api.integration.Package> packages) throws IOException {
        JSON.serialize(packages, Path.of("github-releases.json"));
    }
}
