package io.oscript.hub.api.integration;

import io.oscript.hub.api.config.HubConfiguration;
import io.oscript.hub.api.integration.github.GithubIntegration;
import io.oscript.hub.api.services.FileSystemStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ImporterTest {

    static HubConfiguration configuration;

    @BeforeAll
    static void setUp() throws IOException {

        Fixtures.importSettings();
        configuration = Fixtures.getConfiguration();
    }

    @Test
    void importPackages() throws IOException, InterruptedException {
        Importer importer = new Importer();

        GithubIntegration.init(configuration);
        importer.store = new FileSystemStore();
        importer.store.configuration = configuration;
        importer.store.getAllPackages();

        importer.importPackages();
    }
}