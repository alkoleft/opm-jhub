package io.oscript.hub.api.integration;

import io.oscript.hub.api.config.HubConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class GithubReleasesTest {

    static HubConfiguration configuration;

    @BeforeAll
    static void setUp() throws IOException {

        Fixtures.importSettings();
        configuration = Fixtures.getConfiguration();
    }


    @Test
    void stream() {
        var ints = Stream.of(1, 2, 3, 4, 5);
        var all = ints.map(item -> get(item).stream())
                .reduce(Stream::concat).get();
        all.forEach(System.out::println);
    }

    List<Integer> get(int base) {
        List<Integer> result = new ArrayList<>();
        result.add(base * 10);
        result.add(base * 100);

        return result;
    }
}