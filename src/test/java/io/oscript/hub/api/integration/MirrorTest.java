package io.oscript.hub.api.integration;

import io.oscript.hub.api.integration.classicopmhub.ClassicHubIntegration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
class MirrorTest {


    @Test
    void collectInfo() {
    }

//    @Test
//    void packages() throws IOException, InterruptedException {
//        var packages = ClassicHubIntegration.packages("https://hub.oscript.io");
//
//        assertThat(packages)
//                .contains("configor")
//                .contains("1bdd")
//                .contains("Yaspeller")
//                .contains("yadisk-uploader")
//                .contains("RabbitMQ")
//                .contains("ParserFileV8i")
//                .contains("onec-repo-converter")
//                .contains("edt-export-bugs");
//    }

    @Test
    void versions() {
    }

    @Test
    void request() {
    }
}