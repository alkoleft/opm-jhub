package io.oscript.hub.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.oscript.hub.api.integration.github.Repository;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Package {
    String name;
    String description;
    String version;

    @JsonIgnore
    List<Repository> repositories = new ArrayList<>();

    @JsonIgnore
    List<String> versions = new ArrayList<>();

}
