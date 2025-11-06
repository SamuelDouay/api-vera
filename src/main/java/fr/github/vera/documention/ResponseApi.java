package fr.github.vera.documention;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Réponse API générique")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ResponseApi<T> {
    @Schema(description = "Donnée de la réponse", example = "Opération réussie")
    private final T data;
    @Schema(description = "Metadonné de la réponse")
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
