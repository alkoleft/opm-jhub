package io.oscript.hub.api.controllers;

import io.oscript.hub.api.data.Package;
import io.oscript.hub.api.response.Response;
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
public class ListController extends BaseController {

    static final Logger logger = LoggerFactory.getLogger(ListController.class);

    @GetMapping("packages")
    public ResponseEntity<List<Package>> packageList() throws IOException {
        var body = store.getPackages(Constants.defaultChannel);

        return ResponseEntity
                .ok()
                .body(body);

    }

    @GetMapping("packages/{name}")
    public ResponseEntity<Package> packageInfo(@PathVariable("name") String packageName) {
        var body = store.getPackage(packageName, Constants.defaultChannel);

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
