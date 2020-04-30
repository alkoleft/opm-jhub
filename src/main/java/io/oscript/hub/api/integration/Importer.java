package io.oscript.hub.api.integration;

import io.oscript.hub.api.data.Package;
import io.oscript.hub.api.integration.classicopmhub.ClassicHubPackageSource;
import io.oscript.hub.api.integration.github.GitHubPackageSource;
import io.oscript.hub.api.integration.github.GithubIntegration;
import io.oscript.hub.api.integration.github.Release;
import io.oscript.hub.api.integration.github.Repository;
import io.oscript.hub.api.ospx.Metadata;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.services.FileSystemStore;
import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class Importer {

    static final Logger logger = LoggerFactory.getLogger(Importer.class);

    @Autowired
    static FileSystemStore store;

    public static Stream<PackageSource>newReleases(){
        GitHubPackageSource ghSource = new GitHubPackageSource();
        ClassicHubPackageSource chSource = new ClassicHubPackageSource();
        return Stream.concat(ghSource.releases(), chSource.releases())
                .filter(release -> !store.containsVersion(release.getPackageID(), release.getVersion()));

    }
    public void importPackages() throws IOException, InterruptedException {

        GitHubPackageSource ghSource = new GitHubPackageSource();
        ClassicHubPackageSource chSource = new ClassicHubPackageSource();
        var newReleases = Stream.concat(ghSource.releases(), chSource.releases())
                .filter(release -> !store.containsVersion(release.getPackageID(), release.getVersion()));


//        Set<Repository> repositoriesForHandling = new HashSet<>(GithubReleases.findNewRepositories());
//
//        repositoriesForHandling.addAll(GithubReleases.findNewReleases(Integer.MAX_VALUE));
        Set<Repository> repositoriesForHandling = new HashSet<>(GithubIntegration.getRepositories());

        for (Repository rep : repositoriesForHandling) {

            Package pack = store.getPackage(rep.getPackageID());

            if (Common.isNullOrEmpty(rep.getPackageID())) {
                if (rep.getReleases().isEmpty())
                    continue;

                logger.info("Обработка репозитория {}", rep.getFullName());
                for (var release : rep.getReleases()) {
                    if (!needSaveVersion(pack, release.getVersion())) {
                        continue;
                    }

                    var ospxPackage = getPackage(release);
                    if (ospxPackage == null)
                        continue;
                    var metadata = ospxPackage.getMetadata();

                    if (pack == null) {
                        pack = store.getPackage(metadata.getName());
                    }
                    if (pack != null && Common.isNullOrEmpty(rep.getPackageID())) {
                        rep.setPackageID(metadata.getName());
                    }
                    if (!needSavePackage(pack, metadata)) {
                        continue;
                    }
                    logger.info("Сохранение пакета {}@{}", metadata.getName(), metadata.getVersion());
                    store.savePackage(ospxPackage, Constants.defaultChannel);
                    release.setVersion(metadata.getVersion());
                    if (Common.isNullOrEmpty(rep.getPackageID())) {
                        rep.setPackageID(metadata.getName());
                    }
                }
            }
        }

        GithubIntegration.save();
    }


    OspxPackage getPackage(Release release) throws IOException, InterruptedException {
        if (Common.isNullOrEmpty(release.getPackageUrl())) {
            return null;
        }

        logger.info("Загрузка пакета {}", release.getVersion());
        var data = GithubIntegration.download(URI.create(release.getPackageUrl()));
        if (data == null) {
            logger.error("Не удалось загрузить пакет");
            return null;
        }
        return OspxPackage.parse(data);
    }

    boolean needSavePackage(Package pack, Metadata newPackageMetadata) {

        return needSaveVersion(pack, newPackageMetadata.getVersion());
    }

    private boolean needSaveVersion(Package pack, String version) {
        if (pack == null) {
            return true;
        }
        if (Common.isNullOrEmpty(version)) {
            return true;
        }
        return pack.getVersions().stream().filter(item -> item.equalsIgnoreCase(version)).findFirst().isEmpty();
    }
}
