package io.oscript.hub.api.integration.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.oscript.hub.api.integration.VersionBase;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.utils.Common;
import lombok.Data;
import org.kohsuke.github.GHRelease;

import java.io.IOException;
import java.util.Date;

@Data
public class Release implements VersionBase {
    Date date;
    String version;
    String tag;

    String zipUrl;
    String packageUrl;

    @JsonIgnore
    Repository repository;

    public static Release create(GHRelease ghRelease) {
        Release release = new Release();
        release.setTag(ghRelease.getTagName());
        release.setZipUrl(ghRelease.getZipballUrl());
        try {
            release.setDate(ghRelease.getCreatedAt());
        } catch (IOException ignore) {
        }

        try {
            for (var asset : ghRelease.getAssets()) {
                if (asset.getName().endsWith(".ospx")) {
                    release.setPackageUrl(asset.getBrowserDownloadUrl());
                }

            }
        } catch (IOException ignore) {
        }
        return release;
    }

    public static String getVersionFormTag(String tagName) {
        if (tagName.startsWith("v.")) {
            return tagName.substring(2);
        } else if (tagName.startsWith("v")) {
            return tagName.substring(1);
        } else {
            return tagName;
        }
    }

    public int compareTag(String tag) {
        if (Common.isNullOrEmpty(this.tag)) {
            return this.version.compareToIgnoreCase(getVersionFormTag(tag));
        } else {
            return this.tag.compareToIgnoreCase(tag);
        }
    }

    public void setTag(String tag) {
        this.tag = tag;
        if (version == null) {
            version = getVersionFormTag(tag);
        }
    }

    @JsonIgnore
    public OspxPackage getPackage() {
        return null;
    }

}
