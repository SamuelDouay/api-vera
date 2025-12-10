package fr.github.vera.resources;

import fr.github.vera.model.HistoryQuestion;
import fr.github.vera.repository.IHistoryQuestionRepository;
import fr.github.vera.response.ListResponse;
import fr.github.vera.response.Response;
import fr.github.vera.services.BaseService;
import fr.github.vera.services.HistoryQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/history/questions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "History Question", description = "Historique des modifications des questions")
public class HistoryQuestionResource extends BaseResource<HistoryQuestion, Integer, IHistoryQuestionRepository> {

    private final HistoryQuestionService historyQuestionService = new HistoryQuestionService();

    @Override
    protected String getResourcePath() {
        return "/history/questions";
    }

    @Override
    protected BaseService<HistoryQuestion, Integer, IHistoryQuestionRepository> getService() {
        return historyQuestionService;
    }

    @Override
    protected String getResourceName() {
        return "History Question";
    }

    // === ENDPOINTS SPÉCIFIQUES À L'HISTORIQUE DES QUESTIONS ===

    @GET
    @Path("/question/{questionId}")
    @Operation(summary = "Récupérer l'historique d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Question history retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getHistoryByQuestion(@PathParam("questionId") Integer questionId) {
        List<HistoryQuestion> history = historyQuestionService.getHistoryByQuestion(questionId);
        ListResponse<HistoryQuestion> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}")
    @Operation(summary = "Récupérer l'historique des questions d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Survey question history retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getHistoryBySurvey(@PathParam("surveyId") Integer surveyId) {
        List<HistoryQuestion> history = historyQuestionService.getHistoryBySurvey(surveyId);
        ListResponse<HistoryQuestion> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Récupérer l'historique des questions d'un utilisateur")
    @ApiResponse(
            responseCode = "200",
            description = "User question history retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getHistoryByUser(@PathParam("userId") Integer userId) {
        List<HistoryQuestion> history = historyQuestionService.getHistoryByUser(userId);
        ListResponse<HistoryQuestion> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/question/{questionId}/action/{action}")
    @Operation(summary = "Récupérer l'historique d'une question par action")
    @ApiResponse(
            responseCode = "200",
            description = "Question history by action retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getHistoryByQuestionAndAction(
            @PathParam("questionId") Integer questionId,
            @PathParam("action") String action) {

        List<HistoryQuestion> history = historyQuestionService.getHistoryByQuestionAndAction(questionId, action);
        ListResponse<HistoryQuestion> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/question/{questionId}/latest")
    @Operation(summary = "Récupérer les dernières modifications d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Latest question history retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getLatestHistoryByQuestion(
            @PathParam("questionId") Integer questionId,
            @QueryParam("limit") @DefaultValue("10") int limit) {

        List<HistoryQuestion> history = historyQuestionService.getLatestHistoryByQuestion(questionId, limit);
        ListResponse<HistoryQuestion> response = new ListResponse<>(history);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/question/{questionId}/snapshots")
    @Operation(summary = "Récupérer tous les snapshots d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Question snapshots retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getQuestionSnapshots(@PathParam("questionId") Integer questionId) {
        List<String> snapshots = historyQuestionService.getAllSnapshotsByQuestion(questionId);
        ListResponse<String> response = new ListResponse<>(snapshots);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/question/{questionId}/stats")
    @Operation(summary = "Obtenir les statistiques de l'historique d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Question history statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response getQuestionHistoryStats(@PathParam("questionId") Integer questionId) {
        int totalActions = historyQuestionService.countHistoryByQuestion(questionId);
        int createActions = historyQuestionService.countHistoryByAction("CREATE");
        int updateActions = historyQuestionService.countHistoryByAction("UPDATE");

        QuestionHistoryStats stats = new QuestionHistoryStats(totalActions, createActions, updateActions);
        Response<QuestionHistoryStats> response = new Response<>(stats);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @DELETE
    @Path("/question/{questionId}")
    @Operation(summary = "Supprimer l'historique d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Question history deleted successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Question not found",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response deleteHistoryByQuestion(@PathParam("questionId") Integer questionId) {
        boolean success = historyQuestionService.deleteHistoryByQuestion(questionId);
        Response<Boolean> response = new Response<>(success);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    // Méthodes de validation d'accès
    @Override
    protected void validateAccess(Integer id, SecurityContext securityContext) {
        // Implémentez la validation d'accès à l'historique
    }

    @Override
    protected void preCreate(HistoryQuestion entity, SecurityContext securityContext) {
        // Validation avant création
        if (entity.getIdQuestion() == null) {
            throw new BadRequestException("Question ID is required");
        }
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
    protected void preUpdate(Integer id, HistoryQuestion entity, SecurityContext securityContext) {
        // L'historique est généralement en lecture seule
        throw new BadRequestException("History entries cannot be updated");
    }

    @Override
    protected void preDelete(Integer id, SecurityContext securityContext) {
        validateAccess(id, securityContext);
    }

    // Classe interne pour les statistiques
    public static class QuestionHistoryStats {
        private final int totalActions;
        private final int createActions;
        private final int updateActions;

        public QuestionHistoryStats(int totalActions, int createActions, int updateActions) {
            this.totalActions = totalActions;
            this.createActions = createActions;
            this.updateActions = updateActions;
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
    }
}