package io.oscript.hub.api.integration.classicopmhub;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Package {
    String name;
    List<PackageVersion> versions = new ArrayList<>();

    public Package(String name) {
        this.name = name;
    }
}
