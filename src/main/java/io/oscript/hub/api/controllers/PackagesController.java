package io.oscript.hub.api.controllers;

import io.oscript.hub.api.response.Response;
import io.oscript.hub.api.storage.StoredPackageInfo;
import io.oscript.hub.api.storage.StoredVersionInfo;
import io.oscript.hub.api.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
public class PackagesController extends BaseController {

    static final Logger logger = LoggerFactory.getLogger(PackagesController.class);

    @GetMapping("packages")
    public ResponseEntity<List<StoredPackageInfo>> packageList() throws IOException {
        var body = store.getPackages();

        return ResponseEntity
                .ok()
                .body(body);

    }

    @GetMapping("packages/{name}")
    public ResponseEntity<StoredPackageInfo> packageInfo(@PathVariable("name") String packageName) {
        var body = store.getPackage(packageName);

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

    @GetMapping("packages/{name}/{version}")
    public ResponseEntity<StoredVersionInfo> packageVersionInfo(@PathVariable("name")
                                                                        String packageName,
                                                                @PathVariable("version")
                                                                        String version) {
        var body = store.getVersion(packageName, version, Constants.defaultChannel);

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

    @RequestMapping(value = "/push",
            method = RequestMethod.POST)
    public ResponseEntity<Response> pushPackage(InputStream dataStream,
                                                @RequestHeader HttpHeaders headers) throws IOException {
        return pushPackageHandler(dataStream, headers);

    }

    @RequestMapping(value = "/push",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Response> pushPackage(@RequestParam("file") MultipartFile file,
                                                @RequestHeader HttpHeaders headers) throws IOException {
        return pushPackageHandler(file.getInputStream(), headers);
    }


}