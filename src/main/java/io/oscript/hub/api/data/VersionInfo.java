package io.oscript.hub.api.data;

public interface VersionInfo {
    String getName();

    String getVersion();

    default String fullName() {
        return String.format("%s@%s", getName(), getVersion());
    }
}
