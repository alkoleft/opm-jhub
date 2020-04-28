package io.oscript.hub.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class HttpRequest {
    final static Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    public static String request(String serverURL, String resource, String description) throws IOException, InterruptedException {
        String requestUri = String.format("%s/%s", serverURL, resource);

        var request = java.net.http.HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(requestUri))
                .setHeader("channel", "stable")
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            logger.error("Ошибка операции {}. Код ответа {}\nОтвет:{}", description, response.statusCode(), response.body());
            return null;
        }

        return response.body();
    }
    public static String request(String url, String description) throws IOException, InterruptedException {
        var request = java.net.http.HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            logger.error("Ошибка операции {}. Код ответа {}\nОтвет:{}", description, response.statusCode(), response.body());
            return null;
        }

        return response.body();
    }
}
