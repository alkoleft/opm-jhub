package io.oscript.hub.api.data;

import io.oscript.hub.api.ospx.Metadata;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PackageInfo implements IPackageMetadata {

    String name;

    String version;

    String engineVersion;

    String author;

    String authorEmail;

    String description;

    List<Metadata.DependenceInfo> dependencies = new ArrayList<>();

    public PackageInfo() {
    }

    public PackageInfo(String name) {
        this.name = name;
    }
}
