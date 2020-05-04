package io.oscript.hub.api.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChannelInfo {
    String name;
    boolean isDefault;

    @JsonIgnore
    List<StoredPackageInfo> packages = new ArrayList<>();

    public ChannelInfo() {
    }

    public ChannelInfo(String name, boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;
    }
}
