package io.oscript.hub.api.integration.classicopmhub;

import io.oscript.hub.api.integration.PackageSource;
import io.oscript.hub.api.integration.PackagesSource;

import java.util.stream.Stream;

public class ClassicHubPackageSource implements PackagesSource {

    @Override
    public Stream<PackageSource> releases() {
        return Stream.empty();
    }
}
