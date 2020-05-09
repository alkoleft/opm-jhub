package io.oscript.hub.api.utils;

import io.oscript.hub.api.storage.IPackageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Common {

    private Common() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger logger = LoggerFactory.getLogger(Common.class);

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static String packageFileName(IPackageMetadata metadata) {
        return String.format("%s-%s.ospx", metadata.getName(), metadata.getVersion());
    }

    public static String packageFileName(String name, String version) {
        return String.format("%s-%s.ospx", name, version);
    }

    public static void createPath(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                String message = String.format("Ошибка создания каталога %s", path.toAbsolutePath());
                logger.error(message, e);
            }
        }
    }
}
