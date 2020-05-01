package io.oscript.hub.api.integration.classicopmhub;

import io.oscript.hub.api.integration.PackageBase;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Package implements PackageBase {
    String name;
    List<Version> versions = new ArrayList<>();

    public Package(String name) {
        this.name = name;
    }
}
