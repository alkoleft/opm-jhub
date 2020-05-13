package io.oscript.hub.api.ospx;

import io.oscript.hub.api.utils.XML;
import io.oscript.hub.api.utils.ZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OspxPackage {
    private static final Logger logger = LoggerFactory.getLogger(OspxPackage.class);

    public static final String METADATA_XML = "opm-metadata.xml";
    public static final String CONTENT_ZIP = "content.zip";

    ByteArrayInputStream packageRaw;
    ByteArrayInputStream metadataRaw;
    ByteArrayInputStream contentRaw;
    Metadata metadata;

    public static OspxPackage parse(byte[] binary) {

        try {
            OspxPackage packageData = new OspxPackage();

            packageData.packageRaw = new ByteArrayInputStream(binary);
            var contentMap = ZIP.unPuck(packageData.packageRaw);

            packageData.contentRaw = new ByteArrayInputStream(contentMap.get(CONTENT_ZIP));
            packageData.metadataRaw = new ByteArrayInputStream(contentMap.get(METADATA_XML));

            packageData.metadata = XML.deserialize(packageData.metadataRaw, Metadata.class);

            return packageData;
        } catch (Exception e) {
            logger.error("Ошибка разбора пакета", e);
            return null;
        }
    }

    public static OspxPackage parse(InputStream stream) throws IOException {
        return parse(stream.readAllBytes());
    }

    public ByteArrayInputStream getPackageRaw() {
        packageRaw.reset();
        return packageRaw;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
