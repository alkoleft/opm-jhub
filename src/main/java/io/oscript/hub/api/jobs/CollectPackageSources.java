package io.oscript.hub.api.jobs;

import io.oscript.hub.api.integration.PackageSource;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.Iterator;
import java.util.stream.Stream;

public class CollectPackageSources implements ItemReader<PackageSource> {

    Iterator<PackageSource> stream;

    CollectPackageSources(Stream<PackageSource> packageSourceStream) {
        stream = packageSourceStream.iterator();
    }

    @Override
    public PackageSource read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (stream.hasNext()) {
            return stream.next();
        } else {
            return null;
        }
    }
}
