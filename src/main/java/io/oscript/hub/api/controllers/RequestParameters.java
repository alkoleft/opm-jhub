package io.oscript.hub.api.controllers;

import io.oscript.hub.api.utils.Constants;
import lombok.Data;

@Data
public class RequestParameters {

    String channel = Constants.STABLE;
    String oAuthToken;

}
