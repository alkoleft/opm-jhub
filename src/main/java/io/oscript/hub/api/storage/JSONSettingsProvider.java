package io.oscript.hub.api.storage;

import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JSONSettingsProvider {

    private static final Logger logger = LoggerFactory.getLogger(JSONSettingsProvider.class);

    @Value("${hub.workpath}")
    private Path workPath;

    // region Paths

    public Path getSettingsPath() {
        Path path = workPath.resolve("settings");
        Common.createPath(path);

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
            logger.error(String.format("Ошибка сохранения настроек %s", name), e);
            return false;
        }
    }

    //endregion
}
