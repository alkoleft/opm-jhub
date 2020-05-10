package io.oscript.hub.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.oscript.hub.api.config.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JSON {

    static final Logger logger = LoggerFactory.getLogger(JSON.class);

    static ObjectMapper mapper;
    static ObjectWriter writer;

    private JSON() {
        throw new IllegalStateException("Utility class");
    }

    static ObjectMapper getMapper() {
        if (mapper != null)
            return mapper;
        mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return mapper;
    }

    static ObjectWriter getWriter() {
        if (writer != null)
            return writer;
        writer = getMapper()
                .writerWithView(View.Internal.class);

        return writer;
    }

    public static String serialize(Object object) throws JsonProcessingException {
        return getWriter().writeValueAsString(object);
    }

    public static void serialize(Object object, Path file) throws IOException {
        getWriter().writeValue(file.toFile(), object);
    }

    public static <T> T deserialize(Path path, Class<T> type) throws IOException {
        if (Files.notExists(path))
            return null;
        try {
            return getMapper().readValue(path.toFile(), type);
        } catch (IOException ex) {
            String message = String.format("Ошибка разбора json файла %s", path);
            throw new IOException(message, ex);
        }
    }

    public static <T> List<T> deserializeList(Path path, Class<T> type) throws IOException {
        if (Files.notExists(path))
            return new ArrayList<>();
        try {
            ObjectMapper mapper = getMapper();
            CollectionType collectionType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, type);

            return mapper.readValue(path.toFile(), collectionType);
        } catch (IOException ex) {
            String message = String.format("Ошибка разбора json файла %s", path);
            throw new IOException(message, ex);
        }
    }
}
