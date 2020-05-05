package io.oscript.hub.api.config;

import io.oscript.hub.api.storage.FileSystemStoreProvider;
import io.oscript.hub.api.storage.IStoreProvider;
import io.oscript.hub.api.storage.JSONSettingsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public IStoreProvider getStore() {
        var store = new FileSystemStoreProvider();
        return store;
    }

    @Bean
    public JSONSettingsProvider getSettings() {
        return new JSONSettingsProvider();
    }
}
