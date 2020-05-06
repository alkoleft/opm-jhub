package io.oscript.hub.api.integration.classicopmhub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassicHubIntegrationTest {

    ClassicHubIntegration hubIntegration;

    @BeforeEach
    void setUp() {
        hubIntegration = new ClassicHubIntegration();
    }

    @Test
    void initialize() {
    }

    @Test
    void sync() {
    }

    @Test
    void downloadPackages() {
    }

    @Test
    void getVersions() {
    }

    @Test
    void downloadVersion() {
    }

    @Test
    void downloadLastVersions() {
    }

    @Test
    void downloadURL() {
    }

    @Test
    void testDownloadURL() {
    }

    @Test
    void packageURL() {
    }

    @Test
    void testPackageURL() {
    }

    @Test
    void loadPackageIDs() {
    }

    @Test
    void loadVersion() {
    }

    @Test
    void versions() {
        String server = ClassicHubConfiguration.defaultConfiguration().servers.get(0);
        var result = ClassicHubIntegration.versions(new Package("opm"), server);
        assertThat(result).isNotNull()
                .doesNotContainNull()
                .contains("0.16.2")
                .contains("0.16.0")
                .contains("0.2.1");
    }

    @Test
    void versionsFromDownload() {
        String server = ClassicHubConfiguration.defaultConfiguration().servers.get(1);
        var result = ClassicHubIntegration.versionsFromDownload(new Package("opm"), server);
        assertThat(result).isNotNull()
                .doesNotContainNull()
                .contains("0.16.2")
                .contains("0.16.0")
                .contains("0.2.1");
    }

    @Test
    void parseVersions() {
    }
}