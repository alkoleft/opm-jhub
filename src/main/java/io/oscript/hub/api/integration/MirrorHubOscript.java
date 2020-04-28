package io.oscript.hub.api.integration;

import io.oscript.hub.api.controllers.ListController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class MirrorHubOscript {

    static final Logger logger = LoggerFactory.getLogger(ListController.class);

    public static void collectInfo(String serverURL) throws IOException, InterruptedException {

        String[] packages = packages(serverURL);

        if (packages == null)
            return;

        for (String packageName : packages) {
            Package aPackage = new Package();
            aPackage.name = packageName;

            versions(aPackage, serverURL);
        }
    }

    public static String[] packages(String serverURL) throws IOException, InterruptedException {
        String response = request(serverURL, "download/list.txt", "Получение списка пакетов с хаба");

        return response == null ? null : Arrays.stream(response.split("\n")).map(String::trim).toArray(String[]::new);
    }

    public static List<Version> versions(Package aPackage, String serverURL) throws IOException, InterruptedException {
        String resource = String.format("package/%s", aPackage.name);

        String response = request(serverURL, resource, "Получение списка версий с хаба");

        Pattern versionsPattern = Pattern.compile("<a.+download\\/(.+)\\/\\1-(.+)\\.ospx\">\\2<\\/a>");

        var matcher = versionsPattern.matcher(response);

        while (matcher.find()) {
            aPackage.versions.add(new Version(matcher.group(0), matcher.group(1)));
        }
        return aPackage.versions;
    }

    static String request(String serverURL, String resource, String description) throws IOException, InterruptedException {
        String requestUri = String.format("%s/%s", serverURL, resource);

        var request = HttpRequest.newBuilder()
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

    static class Package {
        String name;
        List<Version> versions = new ArrayList<>();
    }

    static class Version {
        String name;
        String downloadURL;

        Version(String name, String downloadURL) {
            this.name = name;
            this.downloadURL = downloadURL;
        }
    }
}
