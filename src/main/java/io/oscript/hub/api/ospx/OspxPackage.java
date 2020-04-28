package io.oscript.hub.api.ospx;

import io.oscript.hub.api.utils.XML;
import io.oscript.hub.api.utils.ZIP;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OspxPackage {
    public static final String metadataName = "opm-metadata.xml";
    public static final String contentName = "content.zip";

    ByteArrayInputStream packageRaw;
    ByteArrayInputStream metadataRaw;
    ByteArrayInputStream contentRaw;
    Metadata metadata;

    protected OspxPackage() {
    }

    public static OspxPackage parse(byte[] binary) throws IOException {

        OspxPackage packageData = new OspxPackage();

        packageData.packageRaw = new ByteArrayInputStream(binary);
        var contentMap = ZIP.unpuck(packageData.packageRaw);

        packageData.contentRaw = new ByteArrayInputStream(contentMap.get(contentName));
        packageData.metadataRaw = new ByteArrayInputStream(contentMap.get(metadataName));

        packageData.metadata = XML.deserialize(packageData.metadataRaw, Metadata.class);

        return packageData;
    }

    public static OspxPackage parse(InputStream stream) throws IOException {
       return parse(stream.readAllBytes());
    }

    public ByteArrayInputStream getPackageRaw() {
        packageRaw.reset();
        return packageRaw;
    }

    public ByteArrayInputStream getMetadataRaw() {
        metadataRaw.reset();
        return metadataRaw;
    }

    public ByteArrayInputStream getContentRaw() {
        contentRaw.reset();
        return contentRaw;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
