package io.oscript.hub.api.jobs;

import io.oscript.hub.api.ospx.OspxPackage;
import org.springframework.batch.item.ItemProcessor;

import java.nio.file.Path;

public class BuildPackageProcessor implements ItemProcessor<Path, OspxPackage> {
    @Override
    public OspxPackage process(Path item) throws Exception {
        return null;
    }
}
