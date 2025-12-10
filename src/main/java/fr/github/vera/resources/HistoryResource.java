package fr.github.vera.resources;

import fr.github.vera.model.History;
import fr.github.vera.repository.IHistoryRepository;
import fr.github.vera.response.ListResponse;
import fr.github.vera.response.Response;
import fr.github.vera.services.BaseService;
import fr.github.vera.services.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "History", description = "Historique des modifications des surveys")
public class HistoryResource extends BaseResource<History, Integer, IHistoryRepository> {

    private final HistoryService historyService = new HistoryService();

    @Override
    protected String getResourcePath() {
        return "/history";
    }

    @Override
    protected BaseService<History, Integer, IHistoryRepository> getService() {
        return historyService;
    }

    @Override
    protected String getResourceName() {
        return "History";
    }

    // === ENDPOINTS SPÉCIFIQUES À L'HISTORIQUE DES SURVEYS ===

    @GET
    @Path("/survey/{surveyId}")
    @Operation(summary = "Récupérer l'historique d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Survey history retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getHistoryBySurvey(@PathParam("surveyId") Integer surveyId) {
        List<History> history = historyService.getHistoryBySurvey(surveyId);
        ListResponse<History> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Récupérer l'historique d'un utilisateur")
    @ApiResponse(
            responseCode = "200",
            description = "User history retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getHistoryByUser(@PathParam("userId") Integer userId) {
        List<History> history = historyService.getHistoryByUser(userId);
        ListResponse<History> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}/action/{action}")
    @Operation(summary = "Récupérer l'historique d'un survey par action")
    @ApiResponse(
            responseCode = "200",
            description = "Survey history by action retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getHistoryBySurveyAndAction(
            @PathParam("surveyId") Integer surveyId,
            @PathParam("action") String action) {

        List<History> history = historyService.getHistoryBySurveyAndAction(surveyId, action);
        ListResponse<History> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/latest")
    @Operation(summary = "Récupérer les dernières activités")
    @ApiResponse(
            responseCode = "200",
            description = "Latest activities retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getLatestActivities(
            @QueryParam("limit") @DefaultValue("50") int limit) {

        List<History> activities = historyService.getLatestActions(limit);
        ListResponse<History> response = new ListResponse<>(activities);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/recent")
    @Operation(summary = "Récupérer les activités récentes")
    @ApiResponse(
            responseCode = "200",
            description = "Recent activities retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getRecentActivities(
            @QueryParam("days") @DefaultValue("7") int days) {

        List<History> activities = historyService.getRecentActivity(days);
        ListResponse<History> response = new ListResponse<>(activities);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}/latest")
    @Operation(summary = "Récupérer les dernières modifications d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Latest survey history retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getLatestHistoryBySurvey(
            @PathParam("surveyId") Integer surveyId,
            @QueryParam("limit") @DefaultValue("10") int limit) {

        List<History> history = historyService.getLatestHistoryBySurvey(surveyId, limit);
        ListResponse<History> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}/snapshots")
    @Operation(summary = "Récupérer tous les snapshots d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Survey snapshots retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getSurveySnapshots(@PathParam("surveyId") Integer surveyId) {
        List<String> snapshots = historyService.getAllSnapshotsBySurvey(surveyId);
        ListResponse<String> response = new ListResponse<>(snapshots);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}/stats")
    @Operation(summary = "Obtenir les statistiques de l'historique d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Survey history statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response getSurveyHistoryStats(@PathParam("surveyId") Integer surveyId) {
        int totalActions = historyService.countHistoryBySurvey(surveyId);
        int createActions = historyService.countHistoryByAction("CREATE");
        int updateActions = historyService.countHistoryByAction("UPDATE");
        int activateActions = historyService.countHistoryByAction("ACTIVATE");
        int deactivateActions = historyService.countHistoryByAction("DEACTIVATE");

        SurveyHistoryStats stats = new SurveyHistoryStats(totalActions, createActions, updateActions, activateActions, deactivateActions);
        Response<SurveyHistoryStats> response = new Response<>(stats);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/actions")
    @Operation(summary = "Récupérer l'historique par actions spécifiques")
    @ApiResponse(
            responseCode = "200",
            description = "History by actions retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getHistoryByActions(@QueryParam("actions") List<String> actions) {
        List<History> history = historyService.getHistoryByActions(actions);
        ListResponse<History> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @DELETE
    @Path("/survey/{surveyId}")
    @Operation(summary = "Supprimer l'historique d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Survey history deleted successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Survey not found",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response deleteHistoryBySurvey(@PathParam("surveyId") Integer surveyId) {
        boolean success = historyService.deleteHistoryBySurvey(surveyId);
        Response<Boolean> response = new Response<>(success);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @POST
    @Path("/record")
    @Operation(summary = "Enregistrer une action manuellement")
    @ApiResponse(
            responseCode = "201",
            description = "Action recorded successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid data",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response recordAction(
            @FormParam("surveyId") Integer surveyId,
            @FormParam("action") String action,
            @FormParam("snapshot") String snapshot,
            @FormParam("userId") Integer userId) {

        History recordedHistory = historyService.recordAction(surveyId, action, snapshot, userId);
        Response<History> response = new Response<>(recordedHistory);
        return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.CREATED)
                .entity(response)
                .build();
    }

    // Méthodes de validation d'accès
    @Override
    protected void validateAccess(Integer id, SecurityContext securityContext) {
        // Implémentez la validation d'accès à l'historique
    }

    @Override
    protected void preCreate(History entity, SecurityContext securityContext) {
        // Validation avant création
        if (entity.getIdSurvey() == null) {
            throw new BadRequestException("Survey ID is required");
        }
        if (entity.getAction() == null || entity.getAction().trim().isEmpty()) {
            throw new BadRequestException("Action is required");
        }
        if (entity.getSnapshot() == null || entity.getSnapshot().trim().isEmpty()) {
            throw new BadRequestException("Snapshot is required");
        }
        if (entity.getIdUser() == null) {
            throw new BadRequestException("User ID is required");
        }
    }

    @Override
    protected void preUpdate(Integer id, History entity, SecurityContext securityContext) {
        // L'historique est généralement en lecture seule
        throw new BadRequestException("History entries cannot be updated");
    }

    @Override
    protected void preDelete(Integer id, SecurityContext securityContext) {
        validateAccess(id, securityContext);
    }

    // Classes internes pour les statistiques
    public static class SurveyHistoryStats {
        private final int totalActions;
        private final int createActions;
        private final int updateActions;
        private final int activateActions;
        private final int deactivateActions;

        public SurveyHistoryStats(int totalActions, int createActions, int updateActions,
                                  int activateActions, int deactivateActions) {
            this.totalActions = totalActions;
            this.createActions = createActions;
            this.updateActions = updateActions;
            this.activateActions = activateActions;
            this.deactivateActions = deactivateActions;
        }

        public int getTotalActions() {
            return totalActions;
        }

        public int getCreateActions() {
            return createActions;
        }

        public int getUpdateActions() {
            return updateActions;
        }

        public int getActivateActions() {
            return activateActions;
        }

        public int getDeactivateActions() {
            return deactivateActions;
        }
    }
}