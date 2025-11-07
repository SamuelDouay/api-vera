package fr.github.vera.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Réponse API générique")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Response<T> {
    @Schema(description = "Donnée de la réponse", example = "Opération réussie")
    private final T data;
    @Schema(description = "Metadonné de la réponse")
    private Map<String, Object> meta;

    public Response(T data) {
        this.data = data;
    }

    public Response(T data, Map<String, Object> meta) {
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
