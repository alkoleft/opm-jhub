package io.oscript.hub.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class XML {

    public static String serialize(Object object) throws JsonProcessingException {
        return new XmlMapper().writeValueAsString(object);
    }

    public static void serialize(Object object, Path file) throws IOException {
        new XmlMapper().writeValue(file.toFile(), object);
    }

    public static <T> T deserialize(InputStream stream, Class<T> type) throws IOException {
        return new XmlMapper().readValue(stream, type);
    }
}
