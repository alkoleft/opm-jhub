package io.oscript.hub.api.response;

public class DataResponse<T> extends SuccessResponse {
    private final T data;

    public DataResponse(T data) {
        super(null);
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
