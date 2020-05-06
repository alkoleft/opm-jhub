package io.oscript.hub.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    public static Response errorResult(String error_message) {
        return new ErrorResponse(error_message);
    }

    public static Response errorResult(Exception e) {
        return new ErrorResponse(e);
    }

    public static Response successResult(String message) {
        return new SuccessResponse(message);
    }

    public static <T> Response successResult(T data) {
        return new DataResponse<>(data);
    }

    private final LocalDateTime timestamp = LocalDateTime.now();

    private final boolean success;

    private final String message;

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return success ? "SUCCESS" : "ERROR";
    }

}
