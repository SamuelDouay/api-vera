package fr.github.vera.repository;

import fr.github.vera.model.Question;

import java.util.List;
import java.util.Optional;

public interface IQuestionRepository extends IRepository<Question, Integer> {
    // Méthodes spécifiques aux questions
    List<Question> findBySurveyId(Integer surveyId);

    List<Question> findBySurveyIdOrdered(Integer surveyId);

    List<Question> findMandatoryQuestions(Integer surveyId);

    List<Question> findQuestionsBySurveyAndMandatory(Integer surveyId, boolean isMandatory);

    // Gestion de l'ordre d'affichage
    boolean updateDisplayOrder(Integer questionId, Integer displayOrder);

    boolean reorderQuestions(Integer surveyId, List<Integer> questionIdsInOrder);

    Integer getMaxDisplayOrder(Integer surveyId);

    // Questions avec réponses correctes (pour les quiz)
    List<Question> findQuizQuestions(Integer surveyId);

    Optional<Question> findByIdWithCorrectAnswer(Integer questionId);

    // Statistiques
    int countBySurveyId(Integer surveyId);

    int countMandatoryBySurveyId(Integer surveyId);

    // Mise à jour spécifique
    boolean updateQuestionTitle(Integer questionId, String title);

    boolean updateQuestionDescription(Integer questionId, String description);

    boolean toggleMandatoryStatus(Integer questionId);

    boolean updateCorrectAnswer(Integer questionId, String correctAnswerJson);

    // Suppression
    boolean deleteBySurveyId(Integer surveyId);
}