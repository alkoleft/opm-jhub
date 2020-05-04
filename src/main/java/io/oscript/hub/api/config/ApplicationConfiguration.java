package io.oscript.hub.api.config;

import io.oscript.hub.api.storage.FileSystemStoreProvider;
import io.oscript.hub.api.storage.IStoreProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public IStoreProvider getStore() {
        var store = new FileSystemStoreProvider();
        return store;
    }
}
