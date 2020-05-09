package io.oscript.hub.api.utils;

import java.security.InvalidParameterException;

public class Naming {

    static final String[] specialChars = new String[]{"\\", "/", "*", "?"};

    private Naming() {
        throw new IllegalStateException("Utility class");
    }

    public static void checkChannelName(String name) {
        if (isInvalid(name))
            throw new InvalidParameterException("Не корректное имя канала");
    }

    public static void checkPackageName(String name) {
        if (isInvalid(name))
            throw new InvalidParameterException("Не корректное имя пакета");
    }

    public static void checkVersion(String version) {
        if (isInvalid(version)) {
            throw new InvalidParameterException("Не корректная версия пакета");
        }
    }

    public static void check(String name, String version) {
        checkPackageName(name);
        checkVersion(version);
    }

    public static void check(String channel, String name, String version) {
        checkChannelName(channel);
        checkPackageName(name);
        checkVersion(version);
    }


    public static boolean isInvalid(String value) {
        for (String specChar : specialChars) {
            if (value.contains(specChar))
                return true;
        }
        return false;
    }
}
