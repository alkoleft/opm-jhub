package io.oscript.hub.api.response;

public class SuccessResponse extends Response {
    public SuccessResponse(String message) {
        super(true, message);
    }

}
