package io.oscript.hub.api.integration.github;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CollectRepositoriesConfig {

    List<String> organizations = new ArrayList<>();
    List<String> users = new ArrayList<>();
    List<String> repositories = new ArrayList<>();

    public CollectRepositoriesConfig() {
    }
}
