package io.oscript.hub.api.services;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.oscript.hub.api.data.PackageInfo;
import io.oscript.hub.api.data.RequestParameters;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.response.Response;
import io.oscript.hub.api.storage.IStore;
import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class Saver {

    @Autowired
    IStore store;

    public Response savePackage(InputStream stream, RequestParameters parameters) throws IOException {
        OspxPackage ospxPackage = OspxPackage.parse(stream);

        String channel = Common.isNullOrEmpty(parameters.getChannel()) ?
                Constants.defaultChannel :
                parameters.getChannel();

        if (store.savePackage(ospxPackage, channel)) {
            return Response.successResult("Пакет успешно сохранен");
        } else {
            return Response.errorResult("Не удалось сохранить пакет");
        }
    }

    public boolean analyze(MultipartFile file) {


        try {
            File saveTo = new File(file.getOriginalFilename()).getAbsoluteFile();
            file.transferTo(saveTo);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean analyze(InputStream stream) {
        return false;
    }

    public PackageInfo getPackageDescription(InputStream stream) throws IOException {
        var data = unpuck(stream);

        XmlMapper mapper = new XmlMapper();
        PackageInfo packageInfo = mapper.readValue(data.get("opm-metadata.xml"), PackageInfo.class);

        return packageInfo;
    }

    protected Map<String, byte[]> unpuck(byte[] data) throws IOException {
        InputStream iStream = new ByteArrayInputStream(data);
        return unpuck(iStream);
    }

    protected Map<String, byte[]> unpuck(InputStream iStream) throws IOException {
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
