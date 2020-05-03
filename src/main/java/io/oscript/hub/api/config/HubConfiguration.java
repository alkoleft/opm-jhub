package io.oscript.hub.api.config;

import io.oscript.hub.api.utils.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class HubConfiguration {
    @Value("${hub.workpath}")
    private Path workPath;

    public Path getWorkPath() {
        return workPath;
    }

    public void setWorkPath(Path workPath) {
        this.workPath = workPath;
    }

    public Path getSettingsPath() {
        Path path = getWorkPath().resolve("settings");
        if (Files.notExists(path)) {

            try {
                Files.createDirectories(path);
            } catch (IOException ignored) {
            }

        }

        return path;
    }

    public InputStream getConfiguration(String name) throws IOException {
        Path path = getSettingsPath().resolve(String.format("%s.json", name));
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        } else {
            return null;
        }
    }

    public boolean saveConfiguration(String name, Object configuration) {
        try {
            Path path = getSettingsPath().resolve(String.format("%s.json", name));
            JSON.serialize(configuration, path);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Path getStorePath() {
        Path path = getWorkPath().resolve("store");
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception ignored) {
            }
        }

        return path;
    }
}
