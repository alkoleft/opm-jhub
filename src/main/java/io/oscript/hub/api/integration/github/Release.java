package io.oscript.hub.api.integration.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.oscript.hub.api.integration.PackageSource;
import io.oscript.hub.api.integration.PackageSourceType;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.utils.Common;
import lombok.Data;
import org.kohsuke.github.GHRelease;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Data
public class Release implements PackageSource {
    Date date;
    String version;
    String tag;

    String zipUrl;
    String packageUrl;

    @JsonIgnore
    Repository repository;

    public void setTag(String tag) {
        this.tag = tag;
        if (version == null) {
            if (tag.startsWith("v")) {
                version = tag.substring(1);
            } else {
                version = tag;
            }
        }
    }

    public OspxPackage getPackage() {
        return null;
    }

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

    @Override
    public PackageSourceType getType() {
        return Common.isNullOrEmpty(packageUrl) ? PackageSourceType.ZipSource : PackageSourceType.BinaryPackage;
    }

    @Override
    @JsonIgnore
    public String getPackageID() {
        return repository.getPackageID();
    }

    @Override
    public InputStream getStream() {
        return null;
    }
}
