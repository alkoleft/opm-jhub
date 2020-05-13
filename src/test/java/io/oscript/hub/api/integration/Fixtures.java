package io.oscript.hub.api.integration;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class Fixtures {

    public static void importSettings() throws IOException {
        Properties prop = new Properties();
        var source = ClassLoader.getSystemResourceAsStream("application.properties");
        Objects.requireNonNull(source);
        prop.load(source);
        prop.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
    }

}
