package io.oscript.hub.api.integration.github;

import io.oscript.hub.api.integration.VersionBase;
import io.oscript.hub.api.utils.VersionComparator;
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

    public static Release create(GHRelease ghRelease) {
        Release release = new Release();
        release.setTag(ghRelease.getTagName());
        release.setZipUrl(ghRelease.getZipballUrl());
        try {
            release.setDate(ghRelease.getCreatedAt());
        } catch (IOException e) {
            GithubIntegration.logger.error("Ошибка разбора даты создания релиза", e);
        }

        try {
            for (var asset : ghRelease.getAssets()) {
                if (asset.getName().endsWith(".ospx")) {
                    release.setPackageUrl(asset.getBrowserDownloadUrl());
                }

            }
        } catch (IOException e) {
            GithubIntegration.logger.error("Ошибка получения списка прикрепленных артефактов релиза", e);
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

    public int compareVersion(String tag) {
        return VersionComparator.compare(version, getVersionFormTag(tag));
    }

    public void setTag(String tag) {
        this.tag = tag;
        if (version == null) {
            version = getVersionFormTag(tag);
        }
    }
}
