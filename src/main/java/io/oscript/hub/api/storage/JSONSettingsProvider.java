package io.oscript.hub.api.storage;

import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JSONSettingsProvider {

    private static final Logger logger = LoggerFactory.getLogger(JSONSettingsProvider.class);

    @Value("${hub.workpath:data}")
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
        Path path = getSettingsPath().resolve(fileName(name));
        if (Files.notExists(path)) {
            return null;
        }

        return JSON.deserialize(path, type);
    }

    public <T> List<T> getConfigurationList(String name, Class<T> type) throws IOException {
        Path path = getSettingsPath().resolve(fileName(name));
        if (Files.notExists(path)) {
            logger.info("Нет сохраненной конфигурации {} ", type.getSimpleName());
            return new ArrayList<>();
        }

        return JSON.deserializeList(path, type);
    }

    public boolean saveConfiguration(String name, Object configuration) {
        try {
            Path path = getSettingsPath().resolve(fileName(name));
            JSON.serialize(configuration, path);
            return true;
        } catch (IOException e) {
            String message = String.format("Ошибка сохранения настроек %s", name);
            logger.error(message, e);
            return false;
        }
    }

    static String fileName(String configName) {
        return String.format("%s.json", configName);
    }

    //endregion
}
