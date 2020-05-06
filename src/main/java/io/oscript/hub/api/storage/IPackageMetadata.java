package io.oscript.hub.api.storage;

import io.oscript.hub.api.ospx.DependenceInfo;

import java.util.List;

public interface IPackageMetadata {
    String getName();

    String getVersion();

    String getEngineVersion();

    String getAuthor();

    String getAuthorEmail();

    String getDescription();

    List<DependenceInfo> getDependencies();
}
