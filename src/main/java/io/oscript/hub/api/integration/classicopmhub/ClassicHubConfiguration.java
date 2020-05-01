package io.oscript.hub.api.integration.classicopmhub;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassicHubConfiguration {
    List<String> servers = new ArrayList<>();

    public ClassicHubConfiguration() {
        servers.add("http://hub.oscript.io");
        servers.add("http://hub.oscript.ru");
    }
}
