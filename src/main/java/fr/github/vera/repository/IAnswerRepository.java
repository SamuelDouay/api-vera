package fr.github.vera.repository;

import fr.github.vera.model.Answer;

import java.util.List;

public interface IAnswerRepository extends IRepository<Answer, Integer> {

    // Méthodes spécifiques aux réponses
    List<Answer> findByQuestionId(Integer questionId);

    List<Answer> findByRespondentId(String respondentId);

    List<Answer> findByQuestionIdAndRespondentId(Integer questionId, String respondentId);

    List<Answer> findBySurveyId(Integer surveyId);

    // Réponses anonymes vs non anonymes
    List<Answer> findAnonymousAnswers(Integer questionId);

    // Réponses correctes (pour les quiz)
    List<Answer> findCorrectAnswers(Integer questionId);

    // Statistiques
    int countByQuestionId(Integer questionId);

    int countBySurveyId(Integer surveyId);

    int countCorrectAnswersByQuestionId(Integer questionId);

    int countAnonymousAnswersByQuestionId(Integer questionId);

    int countByRespondentId(String respondentId);

    // Agrégations et analyses
    Double getAverageScoreByQuestionId(Integer questionId);

    Double getAverageScoreBySurveyId(Integer surveyId);

    // Gestion des réponses
    boolean markAnswerAsCorrect(Integer answerId, boolean isCorrect);

    boolean anonymizeAnswer(Integer answerId);

    // Suppression
    boolean deleteBySurveyId(Integer surveyId);

    // Vérifications
    boolean hasRespondentAnsweredQuestion(String respondentId, Integer questionId);

}