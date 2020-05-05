package io.oscript.hub.api.services;

import io.oscript.hub.api.data.PackageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PackageAnalyzerTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void analyze() {
    }

    @Test
    void testAnalyze() {
    }

    @ParameterizedTest
    @ValueSource(strings = {"1connector-1.2.3.ospx", "autodocgen-1.0.3.ospx"})
    void unpuck(String path) throws IOException {
//        File file = new File("c:\\tmp", path);
//
//        FileInputStream stream = new FileInputStream(file);
//
//        Saver packageAnalyzer = new Saver();
//        var result = packageAnalyzer.unpuck(stream);
//
//        assertThat(result)
//                .containsKey("opm-metadata.xml")
//                .containsKey("content.zip");

    }

    @ParameterizedTest
    @ValueSource(strings = {"cli-0.9.10.ospx", "autodocgen-1.0.3.ospx"})
    void getPackageDescription(String path) throws IOException {
//        File file = new File("c:\\tmp", path);
//        FileInputStream stream = new FileInputStream(file);
//
//        Saver packageAnalyzer = new Saver();
//        PackageInfo result = packageAnalyzer.getPackageDescription(stream);
//
//        String[] chunks = path.split("-");
//        String packageName = chunks[0];
//        String version = chunks[1].substring(0, chunks[1].length() - 5);
//        assertThat(result.getName()).isEqualTo(packageName);
//        assertThat(result.getVersion()).isEqualTo(version);
//        assertThat(result.getDependencies().size()).isGreaterThan(1);

    }
}