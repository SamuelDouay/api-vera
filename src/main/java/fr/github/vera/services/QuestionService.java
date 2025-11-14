package fr.github.vera.services;

import fr.github.vera.model.Question;
import fr.github.vera.repository.IQuestionRepository;
import fr.github.vera.repository.QuestionRepository;

import java.util.List;
import java.util.Optional;

public class QuestionService extends BaseService<Question, Integer, IQuestionRepository> {

    public QuestionService() {
        super(new QuestionRepository());
    }

    // Méthodes spécifiques aux questions
    public List<Question> getQuestionsBySurvey(Integer surveyId) {
        return repository.findBySurveyId(surveyId);
    }

    public List<Question> getOrderedQuestionsBySurvey(Integer surveyId) {
        return repository.findBySurveyIdOrdered(surveyId);
    }

    public List<Question> getMandatoryQuestions(Integer surveyId) {
        return repository.findMandatoryQuestions(surveyId);
    }

    public List<Question> getQuizQuestions(Integer surveyId) {
        return repository.findQuizQuestions(surveyId);
    }

    public Optional<Question> getQuestionWithCorrectAnswer(Integer questionId) {
        return repository.findByIdWithCorrectAnswer(questionId);
    }

    // Gestion de l'ordre
    public boolean updateQuestionOrder(Integer questionId, Integer displayOrder) {
        return repository.updateDisplayOrder(questionId, displayOrder);
    }

    public boolean reorderSurveyQuestions(Integer surveyId, List<Integer> questionIdsInOrder) {
        return repository.reorderQuestions(surveyId, questionIdsInOrder);
    }

    public Integer getNextDisplayOrder(Integer surveyId) {
        Integer maxOrder = repository.getMaxDisplayOrder(surveyId);
        return maxOrder + 1;
    }

    // Mise à jour
    public boolean updateTitle(Integer questionId, String title) {
        return repository.updateQuestionTitle(questionId, title);
    }

    public boolean updateDescription(Integer questionId, String description) {
        return repository.updateQuestionDescription(questionId, description);
    }

    public boolean toggleMandatory(Integer questionId) {
        return repository.toggleMandatoryStatus(questionId);
    }

    public boolean updateCorrectAnswer(Integer questionId, String correctAnswerJson) {
        return repository.updateCorrectAnswer(questionId, correctAnswerJson);
    }

    // Statistiques
    public int countQuestionsBySurvey(Integer surveyId) {
        return repository.countBySurveyId(surveyId);
    }

    public int countMandatoryQuestionsBySurvey(Integer surveyId) {
        return repository.countMandatoryBySurveyId(surveyId);
    }

    // Création avec ordre automatique
    @Override
    public Question create(Question question) {
        // Définir l'ordre d'affichage automatiquement si non spécifié
        if (question.getDisplayOrder() == null || question.getDisplayOrder() == 0) {
            Integer nextOrder = getNextDisplayOrder(question.getSurveyId());
            question.setDisplayOrder(nextOrder);
        }
        return super.create(question);
    }

    // Suppression
    public boolean deleteQuestionsBySurvey(Integer surveyId) {
        return repository.deleteBySurveyId(surveyId);
    }
}