package io.oscript.hub.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private HttpRequest() {
        throw new IllegalStateException("Utility class");
    }

    public static String request(String serverURL, String resource, String description) throws IOException, InterruptedException {
        String requestUri = String.format("%s/%s", serverURL, resource);

        var request = java.net.http.HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(requestUri))
                .setHeader("channel", "stable")
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String body = response.body();
            logger.error("Ошибка операции {}. Код ответа {}\nОтвет:{}", description, response.statusCode(), body);
            return null;
        }

        return response.body();
    }

    public static String request(String url, String description) {
        return request(URI.create(url), description, HttpResponse.BodyHandlers.ofString());
    }

    public static byte[] download(URI url) {
        return request(url, "Загрузка файла", HttpResponse.BodyHandlers.ofByteArray());
    }

    public static <T> T request(URI url, String description, HttpResponse.BodyHandler<T> handler) {
        logger.debug("{} {}", description, url);

        var request = java.net.http.HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpResponse<T> response;
        try {
            response = httpClient.send(request, handler);
        } catch (Exception e) {
            String message = String.format("Ошибка операции %s с %s. ", description, url);
            logger.error(message, e);
            return null;
        }
        if (response.statusCode() != 200) {
            String body = response.body().toString();
            logger.error("Ошибка операции {} с {}. Код ответа: {}", description, url, response.statusCode());
            logger.error(body);
            return null;
        }

        return response.body();
    }

}
