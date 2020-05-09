package io.oscript.hub.api.controllers;

import io.oscript.hub.api.exceptions.OperationFailedException;
import io.oscript.hub.api.response.Response;
import io.oscript.hub.api.storage.Channel;
import io.oscript.hub.api.storage.StoredPackageInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.stream.Collectors;

@RestController

public class CompatibilityController extends BaseController {
    @GetMapping("download/list.txt")
    public ResponseEntity<String> packageListTXT(@RequestHeader HttpHeaders headers) throws IOException, OperationFailedException {
        RequestParameters parameters = getRequestParameters(headers);
        Channel channel = store.getChannel(parameters.getChannel());
        var packages = channel.getPackages();
        if (packages == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
        String body = packages.stream()
                .map(StoredPackageInfo::getName)
                .collect(Collectors.joining("\n"));

        return getFileResponse("list.txt", body);
    }

    @GetMapping("download/{name}/{filename:.+\\.ospx}")
    public ResponseEntity<Object> downloadPackage(
            @PathVariable("name") String name,
            @PathVariable("filename") String filename,
            @RequestHeader HttpHeaders headers) throws IOException {
        if (!filename.startsWith(name)) {
            return ResponseEntity
                    .badRequest()
                    .body(Response.errorResult("Не верное имя файла"));
        } else {

            RequestParameters parameters = getRequestParameters(headers);
            Channel channel = store.getChannel(parameters.getChannel());

            String version = filename.substring(name.length());
            if (version.startsWith("-")) {
                version = version.substring(1, version.length() - 5);
            } else {
                version = "latest";
            }

            var data = channel.getVersionData(name, version);

            return getFileResponse(filename, data);
        }
    }

}
