package fr.github.vera.resources;

import fr.github.vera.filters.Secured;
import fr.github.vera.model.Survey;
import fr.github.vera.repository.ISurveyRepository;
import fr.github.vera.response.ListResponse;
import fr.github.vera.response.Response;
import fr.github.vera.services.BaseService;
import fr.github.vera.services.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/survey")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Survey", description = "Gestion des questionnaires")
public class SurveyResource extends BaseResource<Survey, Integer, ISurveyRepository> {

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
    protected String getResourceName() {
        return "Survey";
    }

    @GET
    @Path("/user/{userId}")
    @Secured()
    @Operation(
            summary = "Récupérer les surveys d'un utilisateur",
            description = "Retourne tous les surveys créés par un utilisateur spécifique"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Surveys retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response getSurveysByUser(
            @PathParam("userId") Integer userId,
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @Context SecurityContext securityContext) {

        // Validation d'accès
        validateUserAccess(userId, securityContext);

        List<Survey> surveys = surveyService.getSurveysByUser(userId, limit, offset);
        ListResponse<Survey> response = new ListResponse<>(surveys);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/user/{userId}/status/{status}")
    @Secured()
    @Operation(
            summary = "Récupérer les surveys d'un utilisateur par statut",
            description = "Retourne les surveys d'un utilisateur filtrés par statut actif/inactif"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Surveys retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response getSurveysByUserAndStatus(
            @PathParam("userId") Integer userId,
            @PathParam("status") boolean isActive,
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @Context SecurityContext securityContext) {

        validateUserAccess(userId, securityContext);
        List<Survey> surveys = ((SurveyService) getService()).getSurveysByUserAndStatus(userId, isActive, limit, offset);
        ListResponse<Survey> response = new ListResponse<>(surveys);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/public")
    @Operation(
            summary = "Récupérer les surveys publics",
            description = "Retourne tous les surveys accessibles publiquement"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Public surveys retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getPublicSurveys(
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {

        List<Survey> surveys = surveyService.getPublicSurveys(limit, offset);
        ListResponse<Survey> response = new ListResponse<>(surveys);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/active")
    @Operation(
            summary = "Récupérer les surveys actifs",
            description = "Retourne tous les surveys actifs (is_active = true)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Active surveys retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getActiveSurveys(
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {

        List<Survey> surveys = surveyService.getActiveSurveys(limit, offset);
        ListResponse<Survey> response = new ListResponse<>(surveys);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/quiz")
    @Operation(
            summary = "Récupérer les surveys de type quiz",
            description = "Retourne tous les surveys qui sont des quiz (is_quiz = true)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Quiz surveys retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getQuizSurveys(
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {

        List<Survey> surveys = surveyService.getQuizSurveys(limit, offset);
        ListResponse<Survey> response = new ListResponse<>(surveys);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/user/{userId}/count")
    @Secured()
    @Operation(
            summary = "Compter les surveys d'un utilisateur",
            description = "Retourne le nombre total de surveys créés par un utilisateur"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Count retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response countSurveysByUser(
            @PathParam("userId") Integer userId,
            @Context SecurityContext securityContext) {

        validateUserAccess(userId, securityContext);
        int count = ((SurveyService) getService()).countSurveysByUser(userId);
        Response<Integer> response = new Response<>(count);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/public/count")
    @Operation(
            summary = "Compter les surveys publics",
            description = "Retourne le nombre total de surveys publics actifs"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Count retrieved successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response countPublicSurveys() {
        int count = ((SurveyService) getService()).countPublicSurveys();
        Response<Integer> response = new Response<>(count);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/activate")
    @Secured()
    @Operation(
            summary = "Activer/désactiver un survey",
            description = "Active ou désactive un survey (toggle is_active)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Survey activation status updated",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response toggleActivation(
            @PathParam("id") Integer id,
            @Context SecurityContext securityContext) {

        validateSurveyAccess(id, securityContext);
        Survey updatedSurvey = surveyService.toggleActivation(id);
        Response<Survey> response = new Response<>(updatedSurvey);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/visibility")
    @Secured()
    @Operation(
            summary = "Modifier la visibilité d'un survey",
            description = "Rend un survey public ou privé (toggle is_public)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Survey visibility updated",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response toggleVisibility(
            @PathParam("id") Integer id,
            @Context SecurityContext securityContext) {

        validateSurveyAccess(id, securityContext);
        Survey updatedSurvey = surveyService.toggleVisibility(id);
        Response<Survey> response = new Response<>(updatedSurvey);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/description")
    @Secured()
    @Operation(
            summary = "Modifier la description d'un survey",
            description = "Met à jour la description d'un survey existant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Survey description updated",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response updateDescription(
            @PathParam("id") Integer id,
            @FormParam("description") String description,
            @Context SecurityContext securityContext) {

        validateSurveyAccess(id, securityContext);
        boolean success = ((SurveyService) getService()).updateSurveyDescription(id, description);

        if (success) {
            Survey updatedSurvey = getService().getById(id)
                    .orElseThrow(() -> new NotFoundException("Survey not found after update"));
            Response<Survey> response = new Response<>(updatedSurvey);
            return jakarta.ws.rs.core.Response.ok(response).build();
        } else {
            throw new WebApplicationException("Failed to update survey description",
                    jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PATCH
    @Path("/{id}/name")
    @Secured()
    @Operation(
            summary = "Modifier le nom d'un survey",
            description = "Met à jour le nom d'un survey existant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Survey name updated",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response updateName(
            @PathParam("id") Integer id,
            @FormParam("name") String name,
            @Context SecurityContext securityContext) {

        validateSurveyAccess(id, securityContext);
        boolean success = ((SurveyService) getService()).updateSurveyName(id, name);

        if (success) {
            Survey updatedSurvey = getService().getById(id)
                    .orElseThrow(() -> new NotFoundException("Survey not found after update"));
            Response<Survey> response = new Response<>(updatedSurvey);
            return jakarta.ws.rs.core.Response.ok(response).build();
        } else {
            throw new WebApplicationException("Failed to update survey name",
                    jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{id}/duplicate")
    @Secured()
    @Operation(
            summary = "Dupliquer un survey",
            description = "Crée une copie d'un survey existant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Survey duplicated successfully",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response duplicateSurvey(
            @PathParam("id") Integer id,
            @Context SecurityContext securityContext,
            @Context UriInfo uriInfo) {

        validateSurveyAccess(id, securityContext);
        Survey duplicatedSurvey = surveyService.duplicateSurvey(id);
        Response<Survey> response = new Response<>(duplicatedSurvey);

        return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.CREATED)
                .entity(response)
                .location(URI.create("/survey/" + duplicatedSurvey.getId()))
                .build();
    }

    @GET
    @Path("/token/{token}")
    @Operation(
            summary = "Récupérer un survey par token de partage",
            description = "Retourne un survey via son token de partage"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Survey retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response getSurveyByToken(@PathParam("token") String token) {
        Survey survey = surveyService.getSurveyByToken(token)
                .orElseThrow(() -> new NotFoundException("Survey not found with token: " + token));

        Response<Survey> response = new Response<>(survey);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @POST
    @Path("/{id}/generate-token")
    @Secured()
    @Operation(
            summary = "Générer un token de partage",
            description = "Génère un nouveau token de partage pour un survey"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Share token generated successfully",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response generateShareToken(
            @PathParam("id") Integer id,
            @Context SecurityContext securityContext) {

        validateSurveyAccess(id, securityContext);
        Survey survey = surveyService.generateShareToken(id);
        Response<Survey> response = new Response<>(survey);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}/revoke-token")
    @Secured()
    @Operation(
            summary = "Révoquer le token de partage",
            description = "Supprime le token de partage d'un survey"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Share token revoked successfully",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response revokeShareToken(
            @PathParam("id") Integer id,
            @Context SecurityContext securityContext) {

        validateSurveyAccess(id, securityContext);
        Survey survey = surveyService.revokeShareToken(id);
        Response<Survey> response = new Response<>(survey);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    // === MÉTHODES DE VALIDATION ===

    private void validateUserAccess(Integer userId, SecurityContext securityContext) {
        // Implémentez votre logique de validation d'accès
        // Vérifie que l'utilisateur peut accéder aux surveys de cet userId
        // Exemple :
        // String currentUser = securityContext.getUserPrincipal().getName();
        // if (!currentUser.equals(userId.toString())) {
        //     throw new ForbiddenException("Access denied to user's surveys");
        // }
    }

    private void validateSurveyAccess(Integer surveyId, SecurityContext securityContext) {
        // Implémentez votre logique de validation d'accès
        // Vérifie que l'utilisateur peut modifier ce survey
        // Exemple :
        // Survey survey = surveyService.findById(surveyId)
        //     .orElseThrow(() -> new NotFoundException("Survey not found"));
        // String currentUser = securityContext.getUserPrincipal().getName();
        // if (!survey.getIdUser().toString().equals(currentUser)) {
        //     throw new ForbiddenException("Access denied to this survey");
        // }
    }
}