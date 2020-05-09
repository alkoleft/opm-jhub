package io.oscript.hub.api.integration;

import io.oscript.hub.api.exceptions.OperationFailedException;

public interface PackagesSource {
    void sync() throws OperationFailedException;
}
