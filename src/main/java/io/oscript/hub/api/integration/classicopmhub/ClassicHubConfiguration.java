package io.oscript.hub.api.integration.classicopmhub;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassicHubConfiguration {
    String channel;
    List<String> servers = new ArrayList<>();

    static ClassicHubConfiguration defaultConfiguration() {
        ClassicHubConfiguration conf = new ClassicHubConfiguration();
        conf.channel = "opm-mirror";
        conf.servers.add("http://hub.oscript.io");
        conf.servers.add("http://hub.oscript.ru");

        return conf;
    }
}
