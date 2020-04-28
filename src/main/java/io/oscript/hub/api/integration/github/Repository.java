package io.oscript.hub.api.integration.github;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Repository {
    String fullName;
    String url;
    String packageID;

    List<Release> releases = new ArrayList<>();

    Release maxRelease() {
        String maxVersion = "";
        Release maxRelease = null;
        for (Release release : releases) {
            if (release.getVersion().compareTo(maxVersion) > 0) {
                maxVersion = release.getVersion();
                maxRelease = release;
            }
        }

        return maxRelease;
    }
}
