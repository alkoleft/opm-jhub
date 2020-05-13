package io.oscript.hub.api.exceptions;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public static EntityNotFoundException channelNotFound(String channel) {
        return new EntityNotFoundException(String.format("Не найден канал с именем '%s'", channel));
    }

    public static EntityNotFoundException packageNotFound(String channel, String name) {
        return new EntityNotFoundException(String.format("Не найден пакет '%s' в канале '%s'", name, channel));
    }

    public static EntityNotFoundException versionNotFound(String channel, String name, String version) {
        return new EntityNotFoundException(String.format("Не найден версия пакета '%s@%s' в канале '%s'", name, version, channel));
    }
}
