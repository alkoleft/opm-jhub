package io.oscript.hub.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JSON {

    static final Logger logger = LoggerFactory.getLogger(JSON.class);

    static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return mapper;
    }

    public static String serialize(Object object) throws JsonProcessingException {
        return mapper().writeValueAsString(object);
    }

    public static void serialize(Object object, Path file) throws IOException {
        mapper().writeValue(file.toFile(), object);
    }

    public static <T> T deserialize(Path path, Class<T> type) throws IOException {
        if (Files.notExists(path))
            return null;
        try {
            return mapper().readValue(path.toFile(), type);
        } catch (IOException ex) {
            logger.error("Ошибка разбора json файла " + path.toString(), ex);
            throw ex;
        }
    }

    public static <T> List<T> deserializeList(Path path, Class<T> type) throws IOException {
        if (Files.notExists(path))
            return null;
        try {
            ObjectMapper mapper = mapper();
            CollectionType collectionType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, type);

            return mapper.readValue(path.toFile(), collectionType);
        } catch (IOException ex) {
            logger.error("Ошибка чтения разбора файла " + path.toString(), ex);
            throw ex;
        }
    }
}
