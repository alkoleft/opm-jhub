package io.oscript.hub.api.jobs;

import io.oscript.hub.api.integration.PackageSource;
import io.oscript.hub.api.ospx.OspxPackage;
import org.springframework.batch.item.ItemProcessor;

public class BuildPackageProcessor implements ItemProcessor<PackageSource, OspxPackage> {
    @Override
    public OspxPackage process(PackageSource source) throws Exception {
        return null;
    }
}
