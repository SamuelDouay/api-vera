package fr.github.vera.resources;

import fr.github.vera.model.Answer;
import fr.github.vera.repository.IAnswerRepository;
import fr.github.vera.response.ListResponse;
import fr.github.vera.response.Response;
import fr.github.vera.services.AnswerService;
import fr.github.vera.services.BaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/answers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Answer", description = "Gestion des réponses")
public class AnswerResource extends BaseResource<Answer, Integer, IAnswerRepository> {

    private final AnswerService answerService = new AnswerService();

    @Override
    protected String getResourcePath() {
        return "/answers";
    }

    @Override
    protected BaseService<Answer, Integer, IAnswerRepository> getService() {
        return answerService;
    }

    @Override
    protected String getResourceName() {
        return "Answer";
    }

    // === ENDPOINTS SPÉCIFIQUES AUX RÉPONSES ===

    @GET
    @Path("/question/{questionId}")
    @Operation(summary = "Récupérer les réponses d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Answers retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getAnswersByQuestion(@PathParam("questionId") Integer questionId) {
        List<Answer> answers = answerService.getAnswersByQuestion(questionId);
        ListResponse<Answer> response = new ListResponse<>(answers);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/respondent/{respondentId}")
    @Operation(summary = "Récupérer les réponses d'un répondant")
    @ApiResponse(
            responseCode = "200",
            description = "Respondent answers retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getAnswersByRespondent(@PathParam("respondentId") String respondentId) {
        List<Answer> answers = answerService.getAnswersByRespondent(respondentId);
        ListResponse<Answer> response = new ListResponse<>(answers);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}")
    @Operation(summary = "Récupérer les réponses d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Survey answers retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getAnswersBySurvey(@PathParam("surveyId") Integer surveyId) {
        List<Answer> answers = answerService.getAnswersBySurvey(surveyId);
        ListResponse<Answer> response = new ListResponse<>(answers);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/question/{questionId}/anonymous")
    @Operation(summary = "Récupérer les réponses anonymes d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Anonymous answers retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getAnonymousAnswers(@PathParam("questionId") Integer questionId) {
        List<Answer> answers = answerService.getAnonymousAnswers(questionId);
        ListResponse<Answer> response = new ListResponse<>(answers);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/question/{questionId}/correct")
    @Operation(summary = "Récupérer les réponses correctes d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Correct answers retrieved successfully",
            content = @Content(schema = @Schema(implementation = ListResponse.class))
    )
    public jakarta.ws.rs.core.Response getCorrectAnswers(@PathParam("questionId") Integer questionId) {
        List<Answer> answers = answerService.getCorrectAnswers(questionId);
        ListResponse<Answer> response = new ListResponse<>(answers);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/question/{questionId}/stats")
    @Operation(summary = "Obtenir les statistiques des réponses d'une question")
    @ApiResponse(
            responseCode = "200",
            description = "Answer statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response getQuestionStats(@PathParam("questionId") Integer questionId) {
        int totalAnswers = answerService.countAnswersByQuestion(questionId);
        int correctAnswers = answerService.countCorrectAnswersByQuestion(questionId);
        int anonymousAnswers = answerService.countAnonymousAnswersByQuestion(questionId);
        Double averageScore = answerService.getQuestionAverageScore(questionId);

        QuestionStats stats = new QuestionStats(totalAnswers, correctAnswers, anonymousAnswers, averageScore);
        Response<QuestionStats> response = new Response<>(stats);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/survey/{surveyId}/stats")
    @Operation(summary = "Obtenir les statistiques des réponses d'un survey")
    @ApiResponse(
            responseCode = "200",
            description = "Survey answer statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response getSurveyStats(@PathParam("surveyId") Integer surveyId) {
        int totalAnswers = answerService.countAnswersBySurvey(surveyId);
        Double averageScore = answerService.getSurveyAverageScore(surveyId);

        SurveyStats stats = new SurveyStats(totalAnswers, averageScore);
        Response<SurveyStats> response = new Response<>(stats);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/correct")
    @Operation(summary = "Marquer une réponse comme correcte/incorrecte")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Answer marked successfully",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Answer not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response markAsCorrect(
            @PathParam("id") Integer answerId,
            @FormParam("isCorrect") boolean isCorrect) {

        boolean success = answerService.markAnswerAsCorrect(answerId, isCorrect);
        Response<Boolean> response = new Response<>(success);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/anonymize")
    @Operation(summary = "Anonymiser une réponse")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Answer anonymized successfully",
                    content = @Content(schema = @Schema(implementation = Response.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Answer not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response anonymizeAnswer(@PathParam("id") Integer answerId) {
        boolean success = answerService.anonymizeAnswer(answerId);
        Response<Boolean> response = new Response<>(success);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/check")
    @Operation(summary = "Vérifier si un répondant a déjà répondu à une question")
    @ApiResponse(
            responseCode = "200",
            description = "Check completed successfully",
            content = @Content(schema = @Schema(implementation = Response.class))
    )
    public jakarta.ws.rs.core.Response checkIfRespondentAnswered(
            @QueryParam("respondentId") String respondentId,
            @QueryParam("questionId") Integer questionId) {

        boolean hasAnswered = answerService.hasRespondentAnsweredQuestion(respondentId, questionId);
        Response<Boolean> response = new Response<>(hasAnswered);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    // Méthodes de validation d'accès
    @Override
    protected void validateAccess(Integer id, SecurityContext securityContext) {
        // Implémentez la validation d'accès aux réponses
    }

    @Override
    protected void preCreate(Answer entity, SecurityContext securityContext) {
        // Validation avant création
        if (entity.getIdQuestion() == null) {
            throw new BadRequestException("Question ID is required");
        }
        if (entity.getRespondentId() == null || entity.getRespondentId().trim().isEmpty()) {
            throw new BadRequestException("Respondent ID is required");
        }

        // Vérifier si le répondant a déjà répondu à cette question
        if (answerService.hasRespondentAnsweredQuestion(entity.getRespondentId(), entity.getIdQuestion())) {
            throw new BadRequestException("Respondent has already answered this question");
        }
    }

    @Override
    protected void preUpdate(Integer id, Answer entity, SecurityContext securityContext) {
        validateAccess(id, securityContext);
    }

    @Override
    protected void preDelete(Integer id, SecurityContext securityContext) {
        validateAccess(id, securityContext);
    }

    // Classes internes pour les statistiques
    public static class QuestionStats {
        private final int totalAnswers;
        private final int correctAnswers;
        private final int anonymousAnswers;
        private final Double averageScore;

        public QuestionStats(int totalAnswers, int correctAnswers, int anonymousAnswers, Double averageScore) {
            this.totalAnswers = totalAnswers;
            this.correctAnswers = correctAnswers;
            this.anonymousAnswers = anonymousAnswers;
            this.averageScore = averageScore;
        }

        public int getTotalAnswers() {
            return totalAnswers;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getAnonymousAnswers() {
            return anonymousAnswers;
        }

        public Double getAverageScore() {
            return averageScore;
        }
    }

    public static class SurveyStats {
        private final int totalAnswers;
        private final Double averageScore;

        public SurveyStats(int totalAnswers, Double averageScore) {
            this.totalAnswers = totalAnswers;
            this.averageScore = averageScore;
        }

        public int getTotalAnswers() {
            return totalAnswers;
        }

        public Double getAverageScore() {
            return averageScore;
        }
    }
}