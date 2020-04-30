package io.oscript.hub.api.integration.github;

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
import java.util.ArrayList;
import java.util.List;
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

    public static void sync() {
        try {
            logger.info("Синхронизация с Github");
            findNewRepositories();
            findNewReleases();
            logger.info("Синхронизация с Github завершилась успешно");
        } catch (Exception e) {
            logger.error("Ошибка синхронизации с Github", e);
        }
    }

    static void saveRepositories() throws IOException {
        configuration.saveConfiguration("repositories", repositories);
    }

    public static List<Repository> getRepositories() {
        return repositories;
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
            newRepositories.get().forEach(item -> {
                logger.info("Найден новый репозиторий {}", item.getFullName());
                repositories.add(item);
            });
            saveRepositories();
        }
    }

    public static void findNewReleases() throws IOException {

        boolean needSave = false;

        for (Repository rep : repositories) {
            var newReleases = loadReleases(rep, true);
            needSave |= newReleases.findFirst().isPresent();
            newReleases.forEach(item -> {
                logger.info("Найден новый релиз {}@{}", rep.getFullName(), item.getVersion());
                rep.addRelease(item);
            });
        }

        if (needSave)
            saveRepositories();
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

    static boolean suitableRelease(GHRelease ghRelease, Release lastRelease) {
        if (ghRelease.isDraft() || ghRelease.isPrerelease())
            return false;

        return lastRelease == null || lastRelease.getVersion().compareTo(ghRelease.getTagName()) < 0;
    }

    static Stream<Release> loadReleases(Repository rep, boolean onlyNew) throws IOException {
        Release lastRelease = onlyNew ? rep.maxRelease() : null;
        GHRepository repository = getClient().getRepository(rep.getFullName());

        return StreamSupport.stream(repository.listReleases().spliterator(), false)
                .filter(item -> suitableRelease(item, lastRelease))
                .map(Release::create);
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
}
