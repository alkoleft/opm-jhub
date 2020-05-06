package io.oscript.hub.api.utils;

import io.oscript.hub.api.storage.IPackageMetadata;

public class Common {

    public static  boolean isNullOrEmpty(String value){
        return value==null ||value.isEmpty();
    }

    public static String packageFileName(IPackageMetadata metadata){
        return String.format("%s-%s.ospx", metadata.getName(), metadata.getVersion());
    }

    public static String packageFileName(String name, String version){
        return String.format("%s-%s.ospx", name, version);
    }
}
