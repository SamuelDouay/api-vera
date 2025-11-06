package fr.github.vera.documention;

import io.swagger.v3.oas.annotations.media.Schema;

public class ErrorResponseApi extends ResponseApi<String> {
    public ErrorResponseApi(String data) {
        super(data);
    }

    @Schema(description = "Error")
    @Override
    public String getData() {
        return super.getData();
    }
}
