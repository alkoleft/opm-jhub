package io.oscript.hub.api.integration;

import java.io.InputStream;

public interface PackageSource {

    PackageSourceType getType();

    InputStream getStream();
}
