package fr.github.vera.repository;

import fr.github.vera.model.HistoryQuestion;

import java.util.List;

public interface IHistoryQuestionRepository extends IRepository<HistoryQuestion, Integer> {

    // Méthodes spécifiques à l'historique des questions
    List<HistoryQuestion> findByQuestionId(Integer questionId);

    List<HistoryQuestion> findBySurveyId(Integer surveyId);

    List<HistoryQuestion> findByUserId(Integer userId);

    List<HistoryQuestion> findByQuestionIdAndAction(Integer questionId, String action);

    List<HistoryQuestion> findBySurveyIdAndAction(Integer surveyId, String action);

    // Recherche par période
    List<HistoryQuestion> findByQuestionIdAndPeriod(Integer questionId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<HistoryQuestion> findBySurveyIdAndPeriod(Integer surveyId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    // Statistiques
    int countByQuestionId(Integer questionId);

    int countBySurveyId(Integer surveyId);

    int countByUserId(Integer userId);

    int countByAction(String action);

    // Récupération des dernières actions
    List<HistoryQuestion> findLatestByQuestionId(Integer questionId, int limit);

    List<HistoryQuestion> findLatestBySurveyId(Integer surveyId, int limit);

    List<HistoryQuestion> findLatestByUserId(Integer userId, int limit);

    // Actions de nettoyage
    boolean deleteByQuestionId(Integer questionId);

    boolean deleteBySurveyId(Integer surveyId);

    boolean deleteByUserId(Integer userId);

    boolean deleteOlderThan(java.time.LocalDateTime date);

    // Récupération des snapshots spécifiques
    String findLatestSnapshotByQuestionId(Integer questionId);

    List<String> findAllSnapshotsByQuestionId(Integer questionId);
}