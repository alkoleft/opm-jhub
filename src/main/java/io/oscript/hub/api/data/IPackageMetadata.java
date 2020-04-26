package io.oscript.hub.api.data;

import io.oscript.hub.api.ospx.Metadata;

import java.util.List;

public interface IPackageMetadata {
    String getName();

    String getVersion();

    String getEngineVersion();

    String getAuthor();

    String getAuthorEmail();

    String getDescription();

    List<Metadata.DependenceInfo> getDependencies();
}
