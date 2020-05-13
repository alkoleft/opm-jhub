package io.oscript.hub.api.storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.oscript.hub.api.ospx.DependenceInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata implements IPackageMetadata {

    String name;

    String version;

    String engineVersion;

    String author;

    String authorEmail;

    String description;

    List<DependenceInfo> dependencies = new ArrayList<>();

    public static Metadata create(String name, String version) {
        Metadata result = new Metadata();
        result.name = name;
        result.version = version;

        return result;
    }

}