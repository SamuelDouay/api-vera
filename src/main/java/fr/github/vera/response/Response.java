package fr.github.vera.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse API générique")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Response<T>(
        @Schema(description = "Donnée de la réponse", example = "Opération réussie") T data) {
}
