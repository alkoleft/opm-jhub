package io.oscript.hub.api.integration;

import lombok.Data;

import java.util.List;

public interface PackageBase {
    String getName();

    VersionBase[] getVersions();
}
