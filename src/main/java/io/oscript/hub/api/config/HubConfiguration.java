package io.oscript.hub.api.config;

import io.oscript.hub.api.utils.Common;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
        Common.createPath(path);

        return path;
    }
}
