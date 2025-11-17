package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.HistoryQuestion;

import java.util.List;

public class HistoryQuestionRepository extends BaseRepository<HistoryQuestion, Integer> implements IHistoryQuestionRepository {

    public HistoryQuestionRepository() {
        super("history_question", HistoryQuestion.class);
    }

    @Override
    public List<HistoryQuestion> findByQuestionId(Integer questionId) {
        String sql = "SELECT * FROM history_question WHERE id_question = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY QUESTION ID", questionId);
    }

    @Override
    public List<HistoryQuestion> findBySurveyId(Integer surveyId) {
        String sql = "SELECT * FROM history_question WHERE id_survey = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY SURVEY ID", surveyId);
    }

    @Override
    public List<HistoryQuestion> findByUserId(Integer userId) {
        String sql = "SELECT * FROM history_question WHERE id_user = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY USER ID", userId);
    }

    @Override
    public List<HistoryQuestion> findByQuestionIdAndAction(Integer questionId, String action) {
        String sql = "SELECT * FROM history_question WHERE id_question = ? AND action = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY QUESTION AND ACTION", questionId, action);
    }

    @Override
    public List<HistoryQuestion> findBySurveyIdAndAction(Integer surveyId, String action) {
        String sql = "SELECT * FROM history_question WHERE id_survey = ? AND action = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY SURVEY AND ACTION", surveyId, action);
    }

    @Override
    public List<HistoryQuestion> findByQuestionIdAndPeriod(Integer questionId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        String sql = "SELECT * FROM history_question WHERE id_question = ? AND created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY QUESTION AND PERIOD", questionId, startDate, endDate);
    }

    @Override
    public List<HistoryQuestion> findBySurveyIdAndPeriod(Integer surveyId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        String sql = "SELECT * FROM history_question WHERE id_survey = ? AND created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY SURVEY AND PERIOD", surveyId, startDate, endDate);
    }

    @Override
    public int countByQuestionId(Integer questionId) {
        String sql = "SELECT COUNT(*) FROM history_question WHERE id_question = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT HISTORY BY QUESTION", questionId);
    }

    @Override
    public int countBySurveyId(Integer surveyId) {
        String sql = "SELECT COUNT(*) FROM history_question WHERE id_survey = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT HISTORY BY SURVEY", surveyId);
    }

    @Override
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM history_question WHERE id_user = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT HISTORY BY USER", userId);
    }

    @Override
    public int countByAction(String action) {
        String sql = "SELECT COUNT(*) FROM history_question WHERE action = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT HISTORY BY ACTION", action);
    }

    @Override
    public List<HistoryQuestion> findLatestByQuestionId(Integer questionId, int limit) {
        String sql = "SELECT * FROM history_question WHERE id_question = ? ORDER BY created_at DESC LIMIT ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND LATEST HISTORY BY QUESTION", questionId, limit);
    }

    @Override
    public List<HistoryQuestion> findLatestBySurveyId(Integer surveyId, int limit) {
        String sql = "SELECT * FROM history_question WHERE id_survey = ? ORDER BY created_at DESC LIMIT ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND LATEST HISTORY BY SURVEY", surveyId, limit);
    }

    @Override
    public List<HistoryQuestion> findLatestByUserId(Integer userId, int limit) {
        String sql = "SELECT * FROM history_question WHERE id_user = ? ORDER BY created_at DESC LIMIT ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND LATEST HISTORY BY USER", userId, limit);
    }

    @Override
    public boolean deleteByQuestionId(Integer questionId) {
        String sql = "DELETE FROM history_question WHERE id_question = ?";
        return executeUpdate(sql, "DELETE HISTORY BY QUESTION", questionId) != 0;
    }

    @Override
    public boolean deleteBySurveyId(Integer surveyId) {
        String sql = "DELETE FROM history_question WHERE id_survey = ?";
        return executeUpdate(sql, "DELETE HISTORY BY SURVEY", surveyId) != 0;
    }

    @Override
    public boolean deleteByUserId(Integer userId) {
        String sql = "DELETE FROM history_question WHERE id_user = ?";
        return executeUpdate(sql, "DELETE HISTORY BY USER", userId) != 0;
    }

    @Override
    public boolean deleteOlderThan(java.time.LocalDateTime date) {
        String sql = "DELETE FROM history_question WHERE created_at < ?";
        return executeUpdate(sql, "DELETE OLD HISTORY", date) != 0;
    }

    @Override
    public String findLatestSnapshotByQuestionId(Integer questionId) {
        String sql = "SELECT snapshot FROM history_question WHERE id_question = ? ORDER BY created_at DESC LIMIT 1";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getString("snapshot") : null,
                null, "FIND LATEST SNAPSHOT BY QUESTION", questionId);
    }

    @Override
    public List<String> findAllSnapshotsByQuestionId(Integer questionId) {
        String sql = "SELECT snapshot FROM history_question WHERE id_question = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, rs -> {
            List<String> snapshots = new java.util.ArrayList<>();
            while (rs.next()) {
                snapshots.add(rs.getString("snapshot"));
            }
            return snapshots;
        }, List.of(), "FIND ALL SNAPSHOTS BY QUESTION", questionId);
    }
}