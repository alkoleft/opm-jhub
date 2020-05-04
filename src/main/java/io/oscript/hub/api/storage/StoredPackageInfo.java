package io.oscript.hub.api.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StoredPackageInfo {
    String name;
    String description;
    String version;

    @JsonIgnore
    Metadata metadata;

    @JsonIgnore
    List<String> versions = new ArrayList<>();

    public static StoredPackageInfo create(Metadata metadata) {
        StoredPackageInfo packageInfo = new StoredPackageInfo();
        packageInfo.setMetadata(metadata);

        return packageInfo;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
        this.name = metadata.name;
        this.description = metadata.description;
        this.version = metadata.version;
    }
}
