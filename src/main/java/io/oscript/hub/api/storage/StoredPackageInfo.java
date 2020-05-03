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

}
