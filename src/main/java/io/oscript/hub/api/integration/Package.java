package io.oscript.hub.api.integration;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Package {
    protected String name;
    protected String url;
    protected List<Version> versions = new ArrayList<>();
}
