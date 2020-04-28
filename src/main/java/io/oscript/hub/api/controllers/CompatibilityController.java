package io.oscript.hub.api.controllers;

import io.oscript.hub.api.data.IPackageMetadata;
import io.oscript.hub.api.data.Package;
import io.oscript.hub.api.data.RequestParameters;
import io.oscript.hub.api.response.Response;
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
    public ResponseEntity<String> packageListTXT(@RequestHeader HttpHeaders headers) throws IOException {
        RequestParameters parameters = getRequestParameters(headers);
        String body = store
                .getPackages(parameters.getChannel())
                .stream()
                .map(Package::getName)
                .collect(Collectors.joining("\n"));

        return getFileResponse("list.txt", body);
    }

    @GetMapping("download/{name}/{filename:.+\\.ospx}")
    public ResponseEntity<Object> downloadPackage(
            @PathVariable("name") String name,
            @PathVariable("filename") String filename,
            @RequestHeader HttpHeaders headers) throws IOException {
        if (!filename.startsWith(name)) {
            return getResponse(Response.errorResult("Не верное имя файла"));
        } else {

            RequestParameters parameters = getRequestParameters(headers);
            String version = filename.substring(name.length());
            if (version.startsWith("-")) {
                version = version.substring(1, version.length() - 5);
            } else {
                version = "latest";
            }

            IPackageMetadata metadata = store.getVersion(name, version, parameters.getChannel());

            var data = store.getPackageData(metadata, parameters.getChannel());

            return getFileResponse(filename, data);
        }
    }

}
