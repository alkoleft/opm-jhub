package io.oscript.hub.api.utils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;

public class XML {

    private XML() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> T deserialize(InputStream stream, Class<T> type) throws IOException {
        return new XmlMapper().readValue(stream, type);
    }
}
