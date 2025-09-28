package fr.github.vera.resources;

import java.util.Map;

public class ResponseApi<T> {
    private final T data;
    private Map<String, Object> meta;

    public ResponseApi(T data) {
        this.data = data;
    }

    public ResponseApi(T data, Map<String, Object> meta) {
        this.data = data;
        this.meta = meta;
    }

    public T getData() {
        return data;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }
}
