package fr.github.vera.services;

import fr.github.vera.model.History;
import fr.github.vera.repository.HistoryRepository;
import fr.github.vera.repository.IHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;

public class HistoryService extends BaseService<History, Integer, IHistoryRepository> {

    public HistoryService() {
        super(new HistoryRepository());
    }

    // Méthodes spécifiques à l'historique des surveys
    public List<History> getHistoryBySurvey(Integer surveyId) {
        return repository.findBySurveyId(surveyId);
    }

    public List<History> getHistoryByUser(Integer userId) {
        return repository.findByUserId(userId);
    }

    public List<History> getHistoryBySurveyAndAction(Integer surveyId, String action) {
        return repository.findBySurveyIdAndAction(surveyId, action);
    }

    public List<History> getHistoryByUserAndAction(Integer userId, String action) {
        return repository.findByUserIdAndAction(userId, action);
    }

    public List<History> getHistoryBySurveyAndPeriod(Integer surveyId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findBySurveyIdAndPeriod(surveyId, startDate, endDate);
    }

    public List<History> getHistoryByUserAndPeriod(Integer userId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByUserIdAndPeriod(userId, startDate, endDate);
    }

    // Statistiques
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
    public List<History> getLatestHistoryBySurvey(Integer surveyId, int limit) {
        return repository.findLatestBySurveyId(surveyId, limit);
    }

    public List<History> getLatestHistoryByUser(Integer userId, int limit) {
        return repository.findLatestByUserId(userId, limit);
    }

    public List<History> getLatestActions(int limit) {
        return repository.findLatestActions(limit);
    }

    // Snapshots
    public String getLatestSnapshotBySurvey(Integer surveyId) {
        return repository.findLatestSnapshotBySurveyId(surveyId);
    }

    public List<String> getAllSnapshotsBySurvey(Integer surveyId) {
        return repository.findAllSnapshotsBySurveyId(surveyId);
    }

    // Recherche avancée
    public List<History> getHistoryByActions(List<String> actions) {
        return repository.findByActionIn(actions);
    }

    public List<History> getRecentActivity(int days) {
        return repository.findRecentActivity(days);
    }

    // Nettoyage
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
    public History recordAction(Integer surveyId, String action, String snapshot, Integer userId) {
        History history = new History();
        history.setIdSurvey(surveyId);
        history.setAction(action);
        history.setSnapshot(snapshot);
        history.setIdUser(userId);
        history.setCreatedAt(LocalDateTime.now());

        return create(history);
    }

    // Méthodes pour les actions courantes
    public History recordSurveyCreation(Integer surveyId, String snapshot, Integer userId) {
        return recordAction(surveyId, "CREATE", snapshot, userId);
    }

    public History recordSurveyUpdate(Integer surveyId, String snapshot, Integer userId) {
        return recordAction(surveyId, "UPDATE", snapshot, userId);
    }

    public History recordSurveyDeletion(Integer surveyId, String snapshot, Integer userId) {
        return recordAction(surveyId, "DELETE", snapshot, userId);
    }

    public History recordSurveyActivation(Integer surveyId, String snapshot, Integer userId) {
        return recordAction(surveyId, "ACTIVATE", snapshot, userId);
    }

    public History recordSurveyDeactivation(Integer surveyId, String snapshot, Integer userId) {
        return recordAction(surveyId, "DEACTIVATE", snapshot, userId);
    }
}