package io.oscript.hub.api.controllers;

import io.oscript.hub.api.integration.PackageType;
import io.oscript.hub.api.integration.VersionSourceInfo;
import io.oscript.hub.api.ospx.OspxPackage;
import io.oscript.hub.api.response.ErrorResponse;
import io.oscript.hub.api.response.Response;
import io.oscript.hub.api.storage.SavingPackage;
import io.oscript.hub.api.storage.Storage;
import io.oscript.hub.api.utils.Common;
import io.oscript.hub.api.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class BaseController {


    @Autowired
    Storage store;

    ResponseEntity<Response> getResponse(Response response) {
        if (response instanceof ErrorResponse) {
            return ResponseEntity
                    .badRequest()
                    .body(response);
        } else {
            return ResponseEntity
                    .ok(response);
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

        String channel = parameters.getChannel();
        OspxPackage packageData = OspxPackage.parse(dataStream);

        PackageType type = Objects.equals(channel, Constants.STABLE) ? PackageType.STABLE : PackageType.NIGHT_BUILD;
        SavingPackage savingPackage = new SavingPackage(packageData, type, VersionSourceInfo.MANUAL, channel);

        Response response;
        if (store.getChannel(channel).pushPackage(savingPackage) != null) {
            response = Response.successResult("Пакет успешно сохранен");
        } else {
            response = Response.errorResult("Не удалось сохранить пакет");
        }

        return getResponse(response);

    }

    RequestParameters getRequestParameters(HttpHeaders headers) {

        var parameters = new RequestParameters();

        String channel = firstOrNull(headers.get("channel"));
        String oauthToken = firstOrNull(headers.get("oauth-token"));

        if (!Common.isNullOrEmpty(channel)) {
            parameters.setChannel(channel);
        }
        if (!Common.isNullOrEmpty(oauthToken)) {
            parameters.setOAuthToken(oauthToken);
        }

        return parameters;
    }

    <T> T firstOrNull(List<T> items) {
        if (items == null || items.isEmpty())
            return null;
        else
            return items.get(0);
    }

}
