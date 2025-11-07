package fr.github.vera.response;

import fr.github.vera.model.Count;
import io.swagger.v3.oas.annotations.media.Schema;

public class CountResponse extends Response<Count> {
    public CountResponse(Count data) {
        super(data);
    }

    @Schema(description = "Count")
    @Override
    public Count getData() {
        return super.getData();
    }
}
