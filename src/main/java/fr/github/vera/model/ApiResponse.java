package fr.github.vera.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse API générique")
public class ApiResponse {

    @Schema(description = "Message de la réponse", example = "Opération réussie")
    private String message;

    @Schema(description = "Code de statut", example = "200")
    private int status;

    public ApiResponse() {}

    public ApiResponse(String message) {
        this.message = message;
    }

    public ApiResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    // Getters et Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}