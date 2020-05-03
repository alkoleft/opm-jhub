package io.oscript.hub.api.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Channel {
    String name;
    boolean isDefault;

    @JsonIgnore
    List<StoredPackageInfo> packages = new ArrayList<>();

    public Channel() {
    }

    public Channel(String name, boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;
    }
}
