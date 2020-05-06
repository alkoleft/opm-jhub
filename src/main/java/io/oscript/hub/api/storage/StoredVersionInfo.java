package io.oscript.hub.api.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.oscript.hub.api.integration.VersionSourceInfo;
import lombok.Data;

import java.util.Date;

@Data
public class StoredVersionInfo {
    Metadata metadata;
    VersionSourceInfo source;
    Date saveData;

    @JsonIgnore
    public String getVersion() {
        return metadata.getVersion();
    }

    public static StoredVersionInfo create(SavingPackage pack) {

        io.oscript.hub.api.ospx.Metadata metadata = pack.getPackageData().getMetadata();

        StoredVersionInfo storedVersion = new StoredVersionInfo();

        storedVersion.metadata = new Metadata();
        storedVersion.metadata.setName(metadata.getName());
        storedVersion.metadata.setVersion(metadata.getVersion());
        storedVersion.metadata.setEngineVersion(metadata.getEngineVersion());
        storedVersion.metadata.setAuthor(metadata.getAuthor());
        storedVersion.metadata.setAuthorEmail(metadata.getAuthorEmail());
        storedVersion.metadata.setDescription(metadata.getDescription());
        storedVersion.metadata.setDependencies(metadata.getDependencies());

        storedVersion.source = pack.getSourceInfo();
        storedVersion.saveData = new Date();

        return storedVersion;
    }
}
