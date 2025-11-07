package fr.github.vera.response;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthResponse extends Response<fr.github.vera.model.AuthResponse> {
    public AuthResponse(fr.github.vera.model.AuthResponse data) {
        super(data);
    }

    @Schema(description = "Auth")
    @Override
    public fr.github.vera.model.AuthResponse getData() {
        return super.getData();
    }
}
