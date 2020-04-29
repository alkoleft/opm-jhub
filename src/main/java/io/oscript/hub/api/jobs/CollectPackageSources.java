package io.oscript.hub.api.jobs;

import io.oscript.hub.api.integration.github.Repository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class CollectPackageSources implements ItemReader<Repository> {
    @Override
    public Repository read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return null;
    }
}
