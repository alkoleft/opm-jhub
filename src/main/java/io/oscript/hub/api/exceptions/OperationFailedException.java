package io.oscript.hub.api.exceptions;

public class OperationFailedException extends Exception {
    public OperationFailedException(String operation) {
        super(String.format("Не удалось выполнить '%s'", operation));
    }

    public OperationFailedException(String operation, Throwable cause) {
        super(String.format("Не удалось выполнить '%s'", operation), cause);
    }
}
