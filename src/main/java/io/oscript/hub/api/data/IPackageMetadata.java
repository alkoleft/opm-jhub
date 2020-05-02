package io.oscript.hub.api.data;

import io.oscript.hub.api.ospx.DependenceInfo;

import java.util.List;

public interface IPackageMetadata extends VersionInfo {
    String getEngineVersion();

    String getAuthor();

    String getAuthorEmail();

    String getDescription();

    List<DependenceInfo> getDependencies();
}
