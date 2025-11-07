package fr.github.vera.response;

import fr.github.vera.model.Survey;
import io.swagger.v3.oas.annotations.media.Schema;

public class SurveyResponse extends Response<Survey> {
    public SurveyResponse(Survey data) {
        super(data);
    }

    @Schema(description = "Survey")
    @Override
    public Survey getData() {
        return super.getData();
    }
}
