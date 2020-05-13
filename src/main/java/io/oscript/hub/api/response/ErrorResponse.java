package io.oscript.hub.api.response;

public class ErrorResponse extends Response {

    private final String error;

    public ErrorResponse(Exception error) {
        super(false, null);
        this.error = error.getMessage();
    }

    public ErrorResponse(String error) {
        super(false, null);
        this.error = error;
    }

    public ErrorResponse(String error, String message) {
        super(false, message);
        this.error = error;
    }


    public String getError() {
        return error;
    }
}
