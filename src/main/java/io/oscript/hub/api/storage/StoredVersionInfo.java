package io.oscript.hub.api.storage;

import io.oscript.hub.api.integration.VersionSourceInfo;
import lombok.Data;

import java.util.Date;

@Data
public class StoredVersionInfo {
    Metadata metadata;
    VersionSourceInfo source;
    Date saveData;
}
