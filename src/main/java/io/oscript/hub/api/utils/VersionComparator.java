package io.oscript.hub.api.utils;

import java.util.Objects;

public class VersionComparator {

    private VersionComparator() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean large(String compared, String than) {
        return compare(compared, than) > 0;
    }

    public static int compare(String compared, String than) {
        if (Objects.equals(compared, than))
            return 0;
        if (compared == null) {
            return -1;
        }
        if (than == null) {
            return 1;
        }

        if (isSemVer(compared) && isSemVer(than)) {
            return compareSemVer(compared, than);
        } else {
            return compared.compareToIgnoreCase(than);
        }
    }

    public static boolean isSemVer(String version) {
        return version.matches("[0-9]+(\\.[0-9]+)*");
    }

    public static int compareSemVer(String compared, String than) {
        var comparedParts = compared.split("\\.");
        var thanParts = than.split("\\.");

        int partsLength = Math.max(comparedParts.length, thanParts.length);

        for (int i = 0; i < partsLength; i++) {
            int comparedPart = i < comparedParts.length ? Integer.parseInt(comparedParts[i]) : 0;
            int thanPart = i < thanParts.length ? Integer.parseInt(thanParts[i]) : 0;

            if (comparedPart < thanPart)
                return -1;
            else if (comparedPart > thanPart)
                return 1;
        }

        return 0;
    }
}
