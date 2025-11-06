package fr.github.vera.documention;

import fr.github.vera.model.Count;
import io.swagger.v3.oas.annotations.media.Schema;

public class CountResponseApi extends ResponseApi<Count> {
    public CountResponseApi(Count data) {
        super(data);
    }

    @Schema(description = "Count")
    @Override
    public Count getData() {
        return super.getData();
    }
}
