package io.oscript.hub.api.config;

import com.fasterxml.jackson.databind.MapperFeature;
import io.oscript.hub.api.storage.FileSystemStoreProvider;
import io.oscript.hub.api.storage.IStoreProvider;
import io.oscript.hub.api.storage.JSONSettingsProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public IStoreProvider getStore() {
        return new FileSystemStoreProvider();
    }

    @Bean
    public JSONSettingsProvider getSettings() {
        return new JSONSettingsProvider();
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilder() {
        return jacksonObjectMapperBuilder ->
                jacksonObjectMapperBuilder
                        .featuresToEnable(MapperFeature.DEFAULT_VIEW_INCLUSION);
    }
}
