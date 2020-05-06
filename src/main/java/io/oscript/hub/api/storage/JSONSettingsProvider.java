package io.oscript.hub.api.storage;

import io.oscript.hub.api.utils.JSON;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JSONSettingsProvider {

    @Value("${hub.workpath}")
    private Path workPath;

    // region Paths

    public Path getSettingsPath() {
        Path path = workPath.resolve("settings");
        if (Files.notExists(path)) {

            try {
                Files.createDirectories(path);
            } catch (IOException ignored) {
            }

        }

        return path;
    }

    //endregion

    // region Settings

    public <T> T getConfiguration(String name, Class<T> type) throws IOException {
        Path path = getSettingsPath().resolve(String.format("%s.json", name));
        if (Files.notExists(path)) {
            return null;
        }

        return JSON.deserialize(path, type);
    }

    public <T> List<T> getConfigurationList(String name, Class<T> type) throws IOException {
        Path path = getSettingsPath().resolve(String.format("%s.json", name));
        if (Files.notExists(path)) {
            return null;
        }

        return JSON.deserializeList(path, type);
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

    //endregion
}
