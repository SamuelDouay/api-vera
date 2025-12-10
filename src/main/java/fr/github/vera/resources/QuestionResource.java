package fr.github.vera.resources;

import fr.github.vera.model.Question;
import fr.github.vera.repository.IQuestionRepository;
import fr.github.vera.response.ListResponse;
import fr.github.vera.response.Response;
import fr.github.vera.services.BaseService;
import fr.github.vera.services.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/questions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Question", description = "Gestion des questions")
public class QuestionResource extends BaseResource<Question, Integer, IQuestionRepository> {

    private final QuestionService questionService = new QuestionService();

    @Override
    protected String getResourcePath() {
        return "/questions";
    }

    @Override
    protected BaseService<Question, Integer, IQuestionRepository> getService() {
        return questionService;
    }

    @Override
    protected String getResourceName() {
        return "Question";
    }

    // === ENDPOINTS SPÉCIFIQUES AUX QUESTIONS ===

    @GET
    @Path("/survey/{surveyId}")
    @Operation(summary = "Récupérer les questions d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Questions retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getQuestionsBySurvey(@PathParam("surveyId") Integer surveyId) {
        List<Question> questions = questionService.getQuestionsBySurvey(surveyId);
        ListResponse<Question> response = new ListResponse<>(questions);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}/ordered")
    @Operation(summary = "Récupérer les questions ordonnées d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Ordered questions retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getOrderedQuestionsBySurvey(@PathParam("surveyId") Integer surveyId) {
        List<Question> questions = questionService.getOrderedQuestionsBySurvey(surveyId);
        ListResponse<Question> response = new ListResponse<>(questions);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}/mandatory")
    @Operation(summary = "Récupérer les questions obligatoires d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Mandatory questions retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getMandatoryQuestions(@PathParam("surveyId") Integer surveyId) {
        List<Question> questions = questionService.getMandatoryQuestions(surveyId);
        ListResponse<Question> response = new ListResponse<>(questions);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}/quiz")
    @Operation(summary = "Récupérer les questions de quiz d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Quiz questions retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getQuizQuestions(@PathParam("surveyId") Integer surveyId) {
        List<Question> questions = questionService.getQuizQuestions(surveyId);
        ListResponse<Question> response = new ListResponse<>(questions);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/order")
    @Operation(summary = "Mettre à jour l'ordre d'affichage d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Question order updated successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Question not found",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response updateDisplayOrder(
            @PathParam("id") Integer questionId,
            @FormParam("order") Integer displayOrder) {

        boolean success = questionService.updateQuestionOrder(questionId, displayOrder);
        Response<Boolean> response = new Response<>(success);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @POST
    @Path("/survey/{surveyId}/reorder")
    @Operation(summary = "Réorganiser l'ordre des questions")
    @ApiResponse(
            responseCode = "200",
            description = "Questions reordered successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid order list",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response reorderQuestions(
            @PathParam("surveyId") Integer surveyId,
            List<Integer> questionIdsInOrder) {

        if (questionIdsInOrder == null || questionIdsInOrder.isEmpty()) {
            throw new BadRequestException("Question IDs list cannot be empty");
        }

        boolean success = questionService.reorderSurveyQuestions(surveyId, questionIdsInOrder);
        Response<Boolean> response = new Response<>(success);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/mandatory")
    @Operation(summary = "Basculer le statut obligatoire d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Mandatory status toggled successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Question not found",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response toggleMandatory(@PathParam("id") Integer questionId) {
        boolean success = questionService.toggleMandatory(questionId);
        Response<Boolean> response = new Response<>(success);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/correct-answer")
    @Operation(summary = "Mettre à jour la réponse correcte d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Correct answer updated successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Question not found",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response updateCorrectAnswer(
            @PathParam("id") Integer questionId,
            @FormParam("correctAnswer") String correctAnswerJson) {

        boolean success = questionService.updateCorrectAnswer(questionId, correctAnswerJson);
        Response<Boolean> response = new Response<>(success);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}/stats")
    @Operation(summary = "Obtenir les statistiques des questions d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Question statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response getQuestionStats(@PathParam("surveyId") Integer surveyId) {
        int totalQuestions = questionService.countQuestionsBySurvey(surveyId);
        int mandatoryQuestions = questionService.countMandatoryQuestionsBySurvey(surveyId);

        QuestionStats stats = new QuestionStats(totalQuestions, mandatoryQuestions);
        Response<QuestionStats> response = new Response<>(stats);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    // Méthodes de validation d'accès (à implémenter selon votre logique)
    @Override
    protected void validateAccess(Integer id, SecurityContext securityContext) {
        // Implémentez la validation d'accès aux questions
    }

    @Override
    protected void preCreate(Question entity, SecurityContext securityContext) {
        // Validation avant création
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Question title is required");
        }
        if (entity.getSurveyId() == null) {
            throw new BadRequestException("Survey ID is required");
        }
    }

    @Override
    protected void preUpdate(Integer id, Question entity, SecurityContext securityContext) {
        // Validation avant mise à jour
        validateAccess(id, securityContext);
    }

    @Override
    protected void preDelete(Integer id, SecurityContext securityContext) {
        // Validation avant suppression
        validateAccess(id, securityContext);
    }

    // Classe interne pour les statistiques
    public static class QuestionStats {
        private final int totalQuestions;
        private final int mandatoryQuestions;

        public QuestionStats(int totalQuestions, int mandatoryQuestions) {
            this.totalQuestions = totalQuestions;
            this.mandatoryQuestions = mandatoryQuestions;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public int getMandatoryQuestions() {
            return mandatoryQuestions;
        }
    }
}