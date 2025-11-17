package fr.github.vera.services;

import fr.github.vera.model.HistoryQuestion;
import fr.github.vera.repository.HistoryQuestionRepository;
import fr.github.vera.repository.IHistoryQuestionRepository;

import java.time.LocalDateTime;
import java.util.List;

public class HistoryQuestionService extends BaseService<HistoryQuestion, Integer, IHistoryQuestionRepository> {

    public HistoryQuestionService() {
        super(new HistoryQuestionRepository());
    }

    // Méthodes spécifiques à l'historique des questions
    public List<HistoryQuestion> getHistoryByQuestion(Integer questionId) {
        return repository.findByQuestionId(questionId);
    }

    public List<HistoryQuestion> getHistoryBySurvey(Integer surveyId) {
        return repository.findBySurveyId(surveyId);
    }

    public List<HistoryQuestion> getHistoryByUser(Integer userId) {
        return repository.findByUserId(userId);
    }

    public List<HistoryQuestion> getHistoryByQuestionAndAction(Integer questionId, String action) {
        return repository.findByQuestionIdAndAction(questionId, action);
    }

    public List<HistoryQuestion> getHistoryBySurveyAndAction(Integer surveyId, String action) {
        return repository.findBySurveyIdAndAction(surveyId, action);
    }

    public List<HistoryQuestion> getHistoryByQuestionAndPeriod(Integer questionId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByQuestionIdAndPeriod(questionId, startDate, endDate);
    }

    public List<HistoryQuestion> getHistoryBySurveyAndPeriod(Integer surveyId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findBySurveyIdAndPeriod(surveyId, startDate, endDate);
    }

    // Statistiques
    public int countHistoryByQuestion(Integer questionId) {
        return repository.countByQuestionId(questionId);
    }

    public int countHistoryBySurvey(Integer surveyId) {
        return repository.countBySurveyId(surveyId);
    }

    public int countHistoryByUser(Integer userId) {
        return repository.countByUserId(userId);
    }

    public int countHistoryByAction(String action) {
        return repository.countByAction(action);
    }

    // Dernières actions
    public List<HistoryQuestion> getLatestHistoryByQuestion(Integer questionId, int limit) {
        return repository.findLatestByQuestionId(questionId, limit);
    }

    public List<HistoryQuestion> getLatestHistoryBySurvey(Integer surveyId, int limit) {
        return repository.findLatestBySurveyId(surveyId, limit);
    }

    public List<HistoryQuestion> getLatestHistoryByUser(Integer userId, int limit) {
        return repository.findLatestByUserId(userId, limit);
    }

    // Snapshots
    public String getLatestSnapshotByQuestion(Integer questionId) {
        return repository.findLatestSnapshotByQuestionId(questionId);
    }

    public List<String> getAllSnapshotsByQuestion(Integer questionId) {
        return repository.findAllSnapshotsByQuestionId(questionId);
    }

    // Nettoyage
    public boolean deleteHistoryByQuestion(Integer questionId) {
        return repository.deleteByQuestionId(questionId);
    }

    public boolean deleteHistoryBySurvey(Integer surveyId) {
        return repository.deleteBySurveyId(surveyId);
    }

    public boolean deleteHistoryByUser(Integer userId) {
        return repository.deleteByUserId(userId);
    }

    public boolean deleteOldHistory(LocalDateTime date) {
        return repository.deleteOlderThan(date);
    }

    // Méthode utilitaire pour enregistrer une action
    public HistoryQuestion recordAction(Integer questionId, Integer surveyId, String action,
                                        String snapshot, Integer userId) {
        HistoryQuestion history = new HistoryQuestion();
        history.setIdQuestion(questionId);
        history.setIdSurvey(surveyId);
        history.setAction(action);
        history.setSnapshot(snapshot);
        history.setIdUser(userId);
        history.setCreatedAt(LocalDateTime.now());

        return create(history);
    }
}