package io.oscript.hub.api.data;

import io.oscript.hub.api.utils.Constants;
import lombok.Data;

@Data
public class RequestParameters {

    String channel = Constants.STABLE;
    String oAuthToken;

}
