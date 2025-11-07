package fr.github.vera.response;

import io.swagger.v3.oas.annotations.media.Schema;

public class ErrorResponse extends Response<String> {
    public ErrorResponse(String data) {
        super(data);
    }

    @Schema(description = "Error")
    @Override
    public String getData() {
        return super.getData();
    }
}
