package io.oscript.hub.api.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZIP {

    public static Map<String, byte[]> unpuck(InputStream iStream) throws IOException {

        int bufferSize = 1024;

        ZipInputStream stream = new ZipInputStream(iStream);

        Map<String, byte[]> files = new LinkedHashMap<>();
        ZipEntry entry;
        byte[] buffer = new byte[bufferSize];

        while ((entry = stream.getNextEntry()) != null) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int chunkSize;
            while ((chunkSize = stream.read(buffer, 0, bufferSize)) != -1) {
                output.write(buffer, 0, chunkSize);
            }

            files.put(entry.getName(), output.toByteArray());
            output.close();
        }

        stream.close();

        return files;
    }
}
