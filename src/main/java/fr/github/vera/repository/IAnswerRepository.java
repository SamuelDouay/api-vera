package fr.github.vera.repository;

import fr.github.vera.model.Answer;

import java.util.List;
import java.util.Optional;

public interface IAnswerRepository extends IRepository<Answer, Integer> {

    // Méthodes spécifiques aux réponses
    List<Answer> findByQuestionId(Integer questionId);

    List<Answer> findByRespondentId(String respondentId);

    List<Answer> findByQuestionIdAndRespondentId(Integer questionId, String respondentId);

    List<Answer> findBySurveyId(Integer surveyId);

    // Réponses anonymes vs non anonymes
    List<Answer> findAnonymousAnswers(Integer questionId);

    List<Answer> findNonAnonymousAnswers(Integer questionId);

    List<Answer> findAnswersByAnonymity(Integer questionId, boolean isAnonymous);

    // Réponses correctes (pour les quiz)
    List<Answer> findCorrectAnswers(Integer questionId);

    List<Answer> findIncorrectAnswers(Integer questionId);

    List<Answer> findAnswersByCorrectness(Integer questionId, boolean isCorrect);

    // Statistiques
    int countByQuestionId(Integer questionId);

    int countBySurveyId(Integer surveyId);

    int countCorrectAnswersByQuestionId(Integer questionId);

    int countAnonymousAnswersByQuestionId(Integer questionId);

    int countByRespondentId(String respondentId);

    // Agrégations et analyses
    Double getAverageScoreByQuestionId(Integer questionId);

    Double getAverageScoreBySurveyId(Integer surveyId);

    List<Object[]> getAnswerDistribution(Integer questionId);

    // Gestion des réponses
    boolean updateAnswerContent(Integer answerId, String originalAnswer, String anonymousAnswer);

    boolean markAnswerAsCorrect(Integer answerId, boolean isCorrect);

    boolean anonymizeAnswer(Integer answerId);

    // Suppression
    boolean deleteByQuestionId(Integer questionId);

    boolean deleteByRespondentId(String respondentId);

    boolean deleteBySurveyId(Integer surveyId);

    // Vérifications
    boolean hasRespondentAnsweredQuestion(String respondentId, Integer questionId);

    Optional<Answer> findLatestAnswerByQuestionAndRespondent(Integer questionId, String respondentId);
}