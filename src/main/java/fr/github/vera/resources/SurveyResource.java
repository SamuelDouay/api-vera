package fr.github.vera.resources;

import fr.github.vera.exception.UserNotFoundException;
import fr.github.vera.filters.Secured;
import fr.github.vera.model.Survey;
import fr.github.vera.response.CountResponse;
import fr.github.vera.response.Response;
import fr.github.vera.response.SurveyListResponse;
import fr.github.vera.response.SurveyResponse;
import fr.github.vera.services.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/survey")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Survey", description = "Gestion des questionnaires")
public class SurveyResource {
    private final SurveyService surveyService = new SurveyService();

    @GET
    @Secured(adminOnly = true)
    @Operation(
            summary = "Récupérer tous les questionaires",
            description = "Retourne la liste de tous les questionaires")
    @ApiResponse(
            responseCode = "200",
            description = "Questionnaires retrieved successfully",
            content = @Content(schema = @Schema(implementation = SurveyListResponse.class))
    )
    public jakarta.ws.rs.core.Response getAllSurvey(@QueryParam("limit") @DefaultValue("100") int limit,
                                                    @QueryParam("offset") @DefaultValue("0") int offset) {
        List<Survey> surveys = surveyService.getAllSurvey(limit, offset);
        List<Survey> paginatedSurvey = applyPagination(surveys, limit, offset);
        Map<String, Object> meta = createPaginationMeta(surveys.size(), offset, limit, paginatedSurvey.size());
        SurveyListResponse response = new SurveyListResponse(surveys, meta);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Secured()
    @Operation(
            summary = "Récupérer un questionnaire par ID",
            description = "Retourne un questionnaire spécifique par son ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "questionnaire retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "questionnaire not found",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            )
    })
    public jakarta.ws.rs.core.Response getSurveyById(@PathParam("id") Integer id, @Context SecurityContext securityContext) {
        Survey survey = surveyService.getSurveyById(id)
                .orElseThrow(() -> new UserNotFoundException("Survey not found with ID: " + id));

        SurveyResponse response = new SurveyResponse(survey);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PUT
    @Path("/{id}")
    @Secured()
    @Operation(
            summary = "Mettre à jour un survey",
            description = "Met à jour un survey existant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "survey updated successfully",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "survey not found",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            )
    })
    public jakarta.ws.rs.core.Response updateSurveyById(@PathParam("id") Integer id, @Valid Survey survey, @Context SecurityContext securityContext) {
        Survey updateSurvey = surveyService.updateSurvey(id, survey);
        if (updateSurvey == null) {
            throw new UserNotFoundException("Survey not found");
        }
        SurveyResponse response = new SurveyResponse(updateSurvey);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured(adminOnly = true)
    @Operation(
            summary = "Supprimer un survey",
            description = "Supprime un survey par son ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "survey deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response deleteSurvey(@PathParam("id") Integer id) {
        boolean deleted = surveyService.deleteSurvey(id);
        if (!deleted) {
            throw new UserNotFoundException("Survey not found");
        }

        return jakarta.ws.rs.core.Response.noContent().build();
    }

    @GET
    @Path("/count")
    @Secured(adminOnly = true)
    @Operation(
            summary = "Récupérer le nombre total survey",
            description = "Retourne le nombre total de survey"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "return survey size",
                    content = @Content(schema = @Schema(implementation = CountResponse.class))
            )
    })
    public jakarta.ws.rs.core.Response count() {
        return null;
    }

    @POST
    @Secured(adminOnly = true)
    @Operation(
            summary = "Créer un nouvel survey",
            description = "Crée un nouvel survey avec les données fournies"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "survey created successfully",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid survey data",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            )
    })
    public jakarta.ws.rs.core.Response createSurvey(@Valid Survey survey) {
        Survey createSurvey = surveyService.createSurvey(survey);

        SurveyResponse response = new SurveyResponse(survey);
        return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.CREATED)
                .entity(response)
                .location(URI.create("/survey/" + createSurvey.getId()))
                .build();
    }

    private List<Survey> applyPagination(List<Survey> surveys, int limit, int offset) {
        int start = Math.min(offset, surveys.size());
        int end = Math.min(start + limit, surveys.size());
        return surveys.subList(start, end);
    }

    private Map<String, Object> createPaginationMeta(int total, int offset, int limit, int returned) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("total", total);
        meta.put("offset", offset);
        meta.put("limit", limit);
        meta.put("returned", returned);
        return meta;
    }
}
