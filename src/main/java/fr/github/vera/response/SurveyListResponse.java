package fr.github.vera.response;

import fr.github.vera.model.Survey;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public class SurveyListResponse extends Response<List<Survey>> {

    public SurveyListResponse(List<Survey> data, Map<String, Object> meta) {
        super(data, meta);
    }

    public SurveyListResponse(List<Survey> surveys) {
        super(surveys);
    }

    @Schema(description = "Liste des survey", type = "array")
    @Override
    public List<Survey> getData() {
        return super.getData();
    }
}
