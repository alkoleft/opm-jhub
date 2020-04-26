package io.oscript.hub.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class JSON {
    public static String serialize(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    public static void serialize(Object object, Path file) throws IOException {
        new ObjectMapper().writeValue(file.toFile(), object);
    }

    public static <T> T deserialize(InputStream stream, Class<T> type) throws IOException {
        return new ObjectMapper().readValue(stream, type);
    }

    public static <T> T deserialize(Path path, Class<T> type) {
        try {

            return new ObjectMapper().readValue(path.toFile(), type);
        } catch (IOException ex) {
            return null;
        }
    }
}
