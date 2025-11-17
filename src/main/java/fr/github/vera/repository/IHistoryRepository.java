package fr.github.vera.repository;

import fr.github.vera.model.History;

import java.util.List;

public interface IHistoryRepository extends IRepository<History, Integer> {

    // Méthodes spécifiques à l'historique des surveys
    List<History> findBySurveyId(Integer surveyId);

    List<History> findByUserId(Integer userId);

    List<History> findBySurveyIdAndAction(Integer surveyId, String action);

    List<History> findByUserIdAndAction(Integer userId, String action);

    // Recherche par période
    List<History> findBySurveyIdAndPeriod(Integer surveyId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<History> findByUserIdAndPeriod(Integer userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    // Statistiques
    int countBySurveyId(Integer surveyId);

    int countByUserId(Integer userId);

    int countByAction(String action);

    // Récupération des dernières actions
    List<History> findLatestBySurveyId(Integer surveyId, int limit);

    List<History> findLatestByUserId(Integer userId, int limit);

    List<History> findLatestActions(int limit);

    // Actions de nettoyage
    boolean deleteBySurveyId(Integer surveyId);

    boolean deleteByUserId(Integer userId);

    boolean deleteOlderThan(java.time.LocalDateTime date);

    // Récupération des snapshots spécifiques
    String findLatestSnapshotBySurveyId(Integer surveyId);

    List<String> findAllSnapshotsBySurveyId(Integer surveyId);

    // Recherche avancée
    List<History> findByActionIn(List<String> actions);

    List<History> findRecentActivity(int days);
}