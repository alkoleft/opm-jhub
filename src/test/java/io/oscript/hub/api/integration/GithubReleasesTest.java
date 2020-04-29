package io.oscript.hub.api.integration;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.integration.github.GithubIntegration;
import io.oscript.hub.api.integration.github.GithubSource;
import io.oscript.hub.api.integration.github.GithubSourceType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GithubReleasesTest {

    static HubConfiguration configuration;

    @BeforeAll
    static void setUp() throws IOException {

        Fixtures.importSettings();
        configuration = Fixtures.getConfiguration();
    }

    @Test
    void packages() throws IOException, InterruptedException {
        GithubSource source = new GithubSource(GithubSourceType.Organisation, "oscript-library");

        var packages = GithubIntegration.packages(source);
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
        GithubIntegration.init(configuration);

        GithubIntegration.findNewReleases();

        GithubIntegration.save();
//        var pack = GithubReleases.packageByID(source, "1bdd");
//        var versions = GithubReleases.versions(pack);
//        assertThat(versions).extracting("name")
//                .contains("2.4.2")
//                .doesNotContain("3.0.0-beta2")
//                .contains("1.1")
//                .contains("3.1.0")
//                .doesNotContain("3.0.0-rc.2");

    }

    @Test
    void stream() {
        var ints = Stream.of(1,2,3,4,5);
        var all = ints.map(item -> get(item).stream())
                .reduce(Stream::concat).get();
        all.forEach(System.out::println);
    }

    List<Integer> get(int base) {
        List<Integer> result = new ArrayList<>();
        result.add(base * 10);
        result.add(base * 100);

        return result;
    }
}