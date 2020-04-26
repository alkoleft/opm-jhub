package io.oscript.hub.api.config;

import io.oscript.hub.api.services.FileSystemStore;
import io.oscript.hub.api.services.IStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public IStore getStore() {
        return new FileSystemStore();
    }
}
