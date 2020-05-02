package io.oscript.hub.api.config;

import io.oscript.hub.api.storage.FileSystemStore;
import io.oscript.hub.api.storage.IStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public IStore getStore() {
        var store = new FileSystemStore();
        return store;
    }
}
