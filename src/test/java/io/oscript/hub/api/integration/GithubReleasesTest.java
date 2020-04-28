package io.oscript.hub.api.integration;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.integration.github.GithubReleases;
import io.oscript.hub.api.integration.github.GithubSource;
import io.oscript.hub.api.integration.github.GithubSourceType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GithubReleasesTest {

    static HubConfiguration configuration;

    @BeforeAll
    static void setUp() throws IOException {

        Fixtures.importSettings();
        configuration = Fixtures.getConfiguration();
    }

    @Test
    void collectInfo() throws IOException, InterruptedException {
        GithubSource source = new GithubSource(GithubSourceType.Organisation, "oscript-library");

        var packages = GithubReleases.collectInfo(source);

        System.out.println(packages.size());
        assertThat(packages).extracting("name")
                .contains("configor")
                .contains("1bdd")
                .contains("yaspeller")
                .contains("yadisk-uploader")
                .contains("rabbitmq")
                .contains("onec-repo-converter")
                .contains("edt-export-bugs");
    }

    @Test
    void packages() throws IOException, InterruptedException {
        GithubSource source = new GithubSource(GithubSourceType.Organisation, "oscript-library");

        var packages = GithubReleases.packages(source);
        System.out.println(packages.size());

        assertThat(packages).extracting("name")
                .contains("configor")
                .contains("1bdd")
                .contains("yaspeller")
                .contains("yadisk-uploader")
                .contains("rabbitmq")
                .contains("onec-repo-converter")
                .contains("edt-export-bugs");
    }

    @Test
    void versions() throws IOException {
        GithubReleases.init(configuration);

        GithubReleases.findNewReleases(4500);

        GithubReleases.save();
//        var pack = GithubReleases.packageByID(source, "1bdd");
//        var versions = GithubReleases.versions(pack);
//        assertThat(versions).extracting("name")
//                .contains("2.4.2")
//                .doesNotContain("3.0.0-beta2")
//                .contains("1.1")
//                .contains("3.1.0")
//                .doesNotContain("3.0.0-rc.2");

    }

}