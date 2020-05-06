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
}