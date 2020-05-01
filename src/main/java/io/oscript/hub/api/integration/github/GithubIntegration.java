package io.oscript.hub.api.integration.github;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.integration.PackageBase;
import io.oscript.hub.api.integration.PackagesSource;
import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.JSON;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GithubIntegration implements PackagesSource {

    final static Logger logger = LoggerFactory.getLogger(GithubIntegration.class);
    static GitHub client;

    @Autowired
    HubConfiguration configuration;

    public GithubIntegration() {
        logger.warn("СОЗДАН НОВЫЙ ЭКЗЕМПЛЯР ИНТЕГРАТОРА GITHUB");
    }

    GithubConfig config;
    List<Repository> repositories;

    @PostConstruct
    public void init() throws IOException {
        logger.info("Загрузка настроек");
        var stream = configuration.getConfiguration("github");
        if (stream == null) {
            config = new GithubConfig();
        } else {
            config = JSON.deserialize(stream, GithubConfig.class);
            stream.close();
        }

        logger.info("Загружен настройки {}", JSON.serialize(config));

        stream = configuration.getConfiguration("repositories");
        if (stream == null) {
            repositories = new ArrayList<>();
        } else {
            repositories = JSON.deserializeList(stream, Repository.class);
            stream.close();
            repositories.forEach(repository -> repository.getReleases().forEach(release -> release.repository = repository));
        }

        logger.info("Загружен список репозиториев, {} репозиториев", repositories.size());

    }

    @Override
    public void sync() throws Exception {
        logger.info("Синхронизация с Github");
        findNewRepositories();
        findNewReleases();
        logger.info("Синхронизация с Github завершилась успешно");
    }

    @Override
    public PackageBase[] getPackages() {
        return repositories.toArray(PackageBase[]::new);
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    private void findNewRepositories() throws IOException {
        logger.info("Поиск репозиториев");
        logger.debug("Формирование списка источников");
        var sources = Stream.concat(
                config.organizations.stream().map(item -> {
                    try {
                        return getClient().getOrganization(item);
                    } catch (IOException e) {
                        logger.error("Ошибка поиска организации", e);
                        return null;
                    }
                }),
                config.users.stream().map(item -> {
                    try {
                        return getClient().getUser(item);
                    } catch (IOException e) {
                        logger.error("Ошибка поиска организации", e);
                        return null;
                    }
                }))
                .filter(Objects::nonNull);

        logger.debug("Сканирование источников на предмет новых репозиториев");
        var newRepositories = sources
                .map(this::findNewRepositories)
                .filter(Objects::nonNull)
                .reduce(Stream::concat);

        if (newRepositories.isPresent()) {
            newRepositories.get().forEach(item -> {
                logger.info("Найден новый репозиторий {}", item.getFullName());
                repositories.add(item);
            });
            saveRepositories();
        } else {
            logger.info("Новых репозиториев не найдено");
        }
    }

    private void findNewReleases() throws IOException {

        logger.info("Поиск новых релизов");
        boolean needSave = false;

        for (Repository rep : repositories) {
            var newReleases = loadReleases(rep, true);

            needSave |= newReleases.size() > 0;

            if (newReleases.size() > 0) {
                logger.info("Новые релизы для {}\n{}", rep.getFullName(),
                        String.join("\t\t\n", newReleases.stream().map(Release::getVersion).toArray(CharSequence[]::new)));
            }

            newReleases.forEach(rep::addRelease);
        }

        if (needSave)
            saveRepositories();
    }

    private boolean saveRepositories() {
        return configuration.saveConfiguration("repositories", repositories);
    }

    private Stream<Repository> findNewRepositories(GHPerson person) {
        var repositoriesKeys = repositories.stream().map(Repository::getFullName).collect(Collectors.toSet());
        try {
            return person
                    .getRepositories()
                    .values()
                    .stream()
                    .map(this::analyzeFork)
                    .filter(Objects::nonNull)
                    .reduce(Stream::concat)
                    .orElseGet(Stream::empty)
                    .filter(ghrep -> !repositoriesKeys.contains(ghrep.getFullName()))
                    .map(Repository::create)
                    .filter(Objects::nonNull);

        } catch (Exception e) {
            logger.error("Ошибка обработки репозитория", e);
            return null;
        }
    }

    private List<Release> loadReleases(Repository rep, boolean onlyNew) throws IOException {
        Release lastRelease = null;
        if (onlyNew) {
            lastRelease = rep.maxRelease();
            logger.debug("Загрузка только новых релизов {} (Текущий релиз: {})", rep.getFullName(),
                    lastRelease == null ? "нет" : lastRelease.getVersion());
        }

        GHRepository repository = getClient().getRepository(rep.getFullName());

        List<Release> releases = new ArrayList<>();

        for (var ghRelease : repository.listReleases()) {
            if (ghRelease.isDraft() || (!config.collectPreReleases && ghRelease.isPrerelease()))
                continue;
            if (lastRelease != null && lastRelease.compareTag(ghRelease.getTagName()) >= 0) {
                break;
            }

            releases.add(Release.create(ghRelease));
        }
        return releases;
    }

    private GitHub getClient() throws IOException {
        if (client == null) {
            if (Common.isNullOrEmpty(config.token)) {
                logger.debug("Анонимное подключение к GitHub");
                client = GitHub.connectAnonymously();
            } else {
                logger.debug("Подключение к GitHub по токену");
                client = GitHub.connectUsingOAuth(config.token);
            }
        }
        return client;
    }

    private Stream<GHRepository> analyzeFork(GHRepository repository) {

        GHRepository baseRepository = null;
        String fullName = repository.getFullName();
        try {
            if (config.ifFork != ForkStrategy.NOTHING && repository.isFork()) {

                if (repository.getSource() == null) {
                    baseRepository = getClient().getRepositoryById(String.valueOf(repository.getId())).getSource();
                } else {
                    baseRepository = repository.getSource();
                }

            }

        } catch (IOException e) {
            logger.error("Ошибка поиска основного репозитория (если это форк)", e);
        }

        if (baseRepository == null) {
            return Stream.of(repository);
        }
        switch (config.ifFork) {
            case BOTH:
                logger.debug("{} + {}", fullName, baseRepository.getFullName());
                return Stream.of(repository, baseRepository);
            case SOURCE:
                logger.debug("{} -> {}", fullName, baseRepository.getFullName());
                return Stream.of(baseRepository);
            default:
                return Stream.of(repository);
        }
    }
}
