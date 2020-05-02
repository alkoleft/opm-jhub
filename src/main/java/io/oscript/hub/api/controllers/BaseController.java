package io.oscript.hub.api.controllers;

import io.oscript.hub.api.data.RequestParameters;
import io.oscript.hub.api.response.ErrorResponse;
import io.oscript.hub.api.response.Response;
import io.oscript.hub.api.storage.IStore;
import io.oscript.hub.api.services.Saver;
import io.oscript.hub.api.utils.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BaseController {


    @Autowired
    IStore store;

    @Autowired
    Saver packageSaver;

    <T> ResponseEntity<T> getResponse(Response response) {
        if (response instanceof ErrorResponse) {
            return ResponseEntity
                    .badRequest()
                    .body((T) response);
        } else {
            return ResponseEntity
                    .ok((T) response);
        }
    }

    <T> ResponseEntity<T> getFileResponse(String filename, T body) {
        return ResponseEntity
                .ok()
                .header("content-disposition", String.format("attachment; filename=%s", filename))
                .body(body);

    }

    ResponseEntity<Response> pushPackageHandler(InputStream dataStream, HttpHeaders headers) throws IOException {
        RequestParameters parameters = getRequestParameters(headers);
        var response = packageSaver.savePackage(dataStream, parameters);

        return getResponse(response);

    }

    RequestParameters getRequestParameters(HttpHeaders headers) {

        var parameters = new RequestParameters();

        String channel = firstOrNull(headers.get("channel"));
        String oauthToken = firstOrNull(headers.get("oauth-token"));

        if(!Common.isNullOrEmpty(channel)) {
            parameters.setChannel(channel);
        }
        if(!Common.isNullOrEmpty(oauthToken)) {
            parameters.setOAuthToken(oauthToken);
        }

        return parameters;
    }

    <T> T firstOrNull(List<T> items) {
        if (items == null || items.size() == 0)
            return null;
        else
            return items.get(0);
    }

}
