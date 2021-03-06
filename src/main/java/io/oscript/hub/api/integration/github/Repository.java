package io.oscript.hub.api.integration.github;

import lombok.Data;
import org.kohsuke.github.GHRepository;

import java.util.ArrayList;
import java.util.List;

@Data
public class Repository {
    String fullName;
    String url;
    String packageID;

    List<Release> releases = new ArrayList<>();

    public static Repository create(GHRepository rep) {
        Repository repository = new Repository();
        repository.fullName = rep.getFullName();
        return repository;
    }

    public void addRelease(Release release) {
        releases.add(release);
    }

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
