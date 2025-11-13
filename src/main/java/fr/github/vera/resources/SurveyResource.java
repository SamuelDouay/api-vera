package fr.github.vera.resources;

import fr.github.vera.model.Survey;
import fr.github.vera.repository.ISurveyRepository;
import fr.github.vera.response.SurveyListResponse;
import fr.github.vera.response.SurveyResponse;
import fr.github.vera.services.BaseService;
import fr.github.vera.services.SurveyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.function.Function;

@Path("/survey")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Survey", description = "Gestion des questionnaires")
public class SurveyResource extends BaseResource<Survey, Integer, ISurveyRepository, SurveyResponse, SurveyListResponse> {

    private final SurveyService surveyService = new SurveyService();

    @Override
    protected String getResourcePath() {
        return "/survey";
    }

    @Override
    protected BaseService<Survey, Integer, ISurveyRepository> getService() {
        return surveyService;
    }

    @Override
    protected Function<Survey, SurveyResponse> getResponseMapper() {
        return SurveyResponse::new;
    }

    @Override
    protected Function<List<Survey>, SurveyListResponse> getListResponseMapper() {
        return SurveyListResponse::new;
    }

    @Override
    protected String getResourceName() {
        return "Survey";
    }

    @Override
    protected Integer getId(Survey entity) {
        return entity.getId();
    }
}