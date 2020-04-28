package io.oscript.hub.api.integration;

import io.oscript.hub.api.config.HubConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class Fixtures {

    public static void importSettings() throws IOException {
        Properties prop = new Properties();
        var source = ClassLoader.getSystemResourceAsStream("application.properties");
        prop.load(source);
        prop.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
    }

    public static HubConfiguration getConfiguration() {
        var configuration = new HubConfiguration();
        configuration.setWorkPath(Path.of(System.getProperty("hub.workpath")));

        return configuration;
    }
}
