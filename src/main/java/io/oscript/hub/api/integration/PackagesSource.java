package io.oscript.hub.api.integration;

import java.util.stream.Stream;

public interface PackagesSource {
    Stream<PackageSource> releases();
}
