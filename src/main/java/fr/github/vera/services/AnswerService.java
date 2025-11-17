package fr.github.vera.services;

import fr.github.vera.model.Answer;
import fr.github.vera.repository.AnswerRepository;
import fr.github.vera.repository.IAnswerRepository;

import java.util.List;
import java.util.Optional;

public class AnswerService extends BaseService<Answer, Integer, IAnswerRepository> {

    public AnswerService() {
        super(new AnswerRepository());
    }

    // Méthodes spécifiques aux réponses
    public List<Answer> getAnswersByQuestion(Integer questionId) {
        return repository.findByQuestionId(questionId);
    }

    public List<Answer> getAnswersByRespondent(String respondentId) {
        return repository.findByRespondentId(respondentId);
    }

    public List<Answer> getAnswersByQuestionAndRespondent(Integer questionId, String respondentId) {
        return repository.findByQuestionIdAndRespondentId(questionId, respondentId);
    }

    public List<Answer> getAnswersBySurvey(Integer surveyId) {
        return repository.findBySurveyId(surveyId);
    }

    // Réponses anonymes
    public List<Answer> getAnonymousAnswers(Integer questionId) {
        return repository.findAnonymousAnswers(questionId);
    }

    public List<Answer> getNonAnonymousAnswers(Integer questionId) {
        return repository.findNonAnonymousAnswers(questionId);
    }

    // Réponses correctes
    public List<Answer> getCorrectAnswers(Integer questionId) {
        return repository.findCorrectAnswers(questionId);
    }

    public List<Answer> getIncorrectAnswers(Integer questionId) {
        return repository.findIncorrectAnswers(questionId);
    }

    // Statistiques
    public int countAnswersByQuestion(Integer questionId) {
        return repository.countByQuestionId(questionId);
    }

    public int countAnswersBySurvey(Integer surveyId) {
        return repository.countBySurveyId(surveyId);
    }

    public int countCorrectAnswersByQuestion(Integer questionId) {
        return repository.countCorrectAnswersByQuestionId(questionId);
    }

    public int countAnonymousAnswersByQuestion(Integer questionId) {
        return repository.countAnonymousAnswersByQuestionId(questionId);
    }

    public int countAnswersByRespondent(String respondentId) {
        return repository.countByRespondentId(respondentId);
    }

    // Analyses
    public Double getQuestionAverageScore(Integer questionId) {
        return repository.getAverageScoreByQuestionId(questionId);
    }

    public Double getSurveyAverageScore(Integer surveyId) {
        return repository.getAverageScoreBySurveyId(surveyId);
    }

    public List<Object[]> getQuestionAnswerDistribution(Integer questionId) {
        return repository.getAnswerDistribution(questionId);
    }

    // Gestion des réponses
    public boolean updateAnswerContent(Integer answerId, String originalAnswer, String anonymousAnswer) {
        return repository.updateAnswerContent(answerId, originalAnswer, anonymousAnswer);
    }

    public boolean markAnswerAsCorrect(Integer answerId, boolean isCorrect) {
        return repository.markAnswerAsCorrect(answerId, isCorrect);
    }

    public boolean anonymizeAnswer(Integer answerId) {
        return repository.anonymizeAnswer(answerId);
    }

    // Vérifications
    public boolean hasRespondentAnsweredQuestion(String respondentId, Integer questionId) {
        return repository.hasRespondentAnsweredQuestion(respondentId, questionId);
    }

    public Optional<Answer> getLatestAnswerByQuestionAndRespondent(Integer questionId, String respondentId) {
        return repository.findLatestAnswerByQuestionAndRespondent(questionId, respondentId);
    }

    // Suppression
    public boolean deleteAnswersByQuestion(Integer questionId) {
        return repository.deleteByQuestionId(questionId);
    }

    public boolean deleteAnswersByRespondent(String respondentId) {
        return repository.deleteByRespondentId(respondentId);
    }

    public boolean deleteAnswersBySurvey(Integer surveyId) {
        return repository.deleteBySurveyId(surveyId);
    }

    // Méthode utilitaire pour calculer le score
    public Double calculateRespondentScore(String respondentId, Integer surveyId) {
        // Implémentation pour calculer le score d'un répondant pour un survey
        String sql = """
                SELECT AVG(CASE WHEN a.is_correct THEN 1 ELSE 0 END) 
                FROM answer a 
                JOIN question q ON a.id_question = q.id 
                WHERE a.respondent_id = ? AND q.id_survey = ? AND a.is_correct IS NOT NULL
                """;
        // Cette méthode nécessiterait un accès direct à la base de données
        // Vous pouvez l'implémenter dans le repository si nécessaire
        return 0.0;
    }
}