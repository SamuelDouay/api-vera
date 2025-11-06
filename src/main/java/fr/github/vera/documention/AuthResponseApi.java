package fr.github.vera.documention;

import fr.github.vera.model.AuthResponse;
import io.swagger.v3.oas.annotations.media.Schema;

public class AuthResponseApi extends ResponseApi<AuthResponse> {
    public AuthResponseApi(AuthResponse data) {
        super(data);
    }

    @Schema(description = "Auth")
    @Override
    public AuthResponse getData() {
        return super.getData();
    }
}
