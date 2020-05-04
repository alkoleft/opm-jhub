package io.oscript.hub.api.controllers;

import io.oscript.hub.api.storage.ChannelInfo;
import io.oscript.hub.api.storage.StoredPackageInfo;
import io.oscript.hub.api.storage.StoredVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("channels")
public class ChannelsController extends BaseController {
    static final Logger logger = LoggerFactory.getLogger(ChannelsController.class);

    @GetMapping("/")
    public ResponseEntity<List<ChannelInfo>> list() throws IOException {
        var body = store.getChannels();

        return ResponseEntity
                .ok()
                .body(body);

    }

    @GetMapping("{name}")
    public ResponseEntity<List<StoredPackageInfo>> getItem(@PathVariable("name") String name) throws IOException {
        var body = store.getPackages(name);

        return ResponseEntity
                .ok()
                .body(body);

    }

    @GetMapping("{channel}/{name}")
    public ResponseEntity<StoredPackageInfo> packageInfo(@PathVariable("channel")
                                                                 String channel,
                                                         @PathVariable("name")
                                                                 String packageName) {
        var body = store.getPackage(packageName, channel);

        if (body != null) {
            return ResponseEntity
                    .ok()
                    .body(body);
        } else {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @GetMapping("{channel}/{name}/versions")
    public ResponseEntity<List<StoredVersionInfo>> versions(@PathVariable("channel")
                                                                    String channel,
                                                            @PathVariable("name")
                                                                    String packageName) {
        var body = store.getVersions(packageName, channel);

        if (body != null) {
            return ResponseEntity
                    .ok()
                    .body(body);
        } else {
            return ResponseEntity
                    .notFound()
                    .build();
        }

    }

    @GetMapping("{channel}/{name}/{version}")
    public ResponseEntity<StoredVersionInfo> packageVersionInfo(@PathVariable("channel")
                                                                        String channel,
                                                                @PathVariable("name")
                                                                        String packageName,
                                                                @PathVariable("version")
                                                                        String version) {
        var body = store.getVersion(packageName, version, channel);

        if (body != null) {
            return ResponseEntity
                    .ok()
                    .body(body);
        } else {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

}
