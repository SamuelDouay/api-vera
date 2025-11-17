package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.History;

import java.util.List;

public class HistoryRepository extends BaseRepository<History, Integer> implements IHistoryRepository {

    public HistoryRepository() {
        super("history", History.class);
    }

    @Override
    public List<History> findBySurveyId(Integer surveyId) {
        String sql = "SELECT * FROM history WHERE id_survey = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY SURVEY ID", surveyId);
    }

    @Override
    public List<History> findByUserId(Integer userId) {
        String sql = "SELECT * FROM history WHERE id_user = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY USER ID", userId);
    }

    @Override
    public List<History> findBySurveyIdAndAction(Integer surveyId, String action) {
        String sql = "SELECT * FROM history WHERE id_survey = ? AND action = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY SURVEY AND ACTION", surveyId, action);
    }

    @Override
    public List<History> findByUserIdAndAction(Integer userId, String action) {
        String sql = "SELECT * FROM history WHERE id_user = ? AND action = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY USER AND ACTION", userId, action);
    }

    @Override
    public List<History> findBySurveyIdAndPeriod(Integer surveyId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        String sql = "SELECT * FROM history WHERE id_survey = ? AND created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY SURVEY AND PERIOD", surveyId, startDate, endDate);
    }

    @Override
    public List<History> findByUserIdAndPeriod(Integer userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        String sql = "SELECT * FROM history WHERE id_user = ? AND created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY USER AND PERIOD", userId, startDate, endDate);
    }

    @Override
    public int countBySurveyId(Integer surveyId) {
        String sql = "SELECT COUNT(*) FROM history WHERE id_survey = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT HISTORY BY SURVEY", surveyId);
    }

    @Override
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM history WHERE id_user = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT HISTORY BY USER", userId);
    }

    @Override
    public int countByAction(String action) {
        String sql = "SELECT COUNT(*) FROM history WHERE action = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT HISTORY BY ACTION", action);
    }

    @Override
    public List<History> findLatestBySurveyId(Integer surveyId, int limit) {
        String sql = "SELECT * FROM history WHERE id_survey = ? ORDER BY created_at DESC LIMIT ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND LATEST HISTORY BY SURVEY", surveyId, limit);
    }

    @Override
    public List<History> findLatestByUserId(Integer userId, int limit) {
        String sql = "SELECT * FROM history WHERE id_user = ? ORDER BY created_at DESC LIMIT ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND LATEST HISTORY BY USER", userId, limit);
    }

    @Override
    public List<History> findLatestActions(int limit) {
        String sql = "SELECT * FROM history ORDER BY created_at DESC LIMIT ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND LATEST ACTIONS", limit);
    }

    @Override
    public boolean deleteBySurveyId(Integer surveyId) {
        String sql = "DELETE FROM history WHERE id_survey = ?";
        return executeUpdate(sql, "DELETE HISTORY BY SURVEY", surveyId) != 0;
    }

    @Override
    public boolean deleteByUserId(Integer userId) {
        String sql = "DELETE FROM history WHERE id_user = ?";
        return executeUpdate(sql, "DELETE HISTORY BY USER", userId) != 0;
    }

    @Override
    public boolean deleteOlderThan(java.time.LocalDateTime date) {
        String sql = "DELETE FROM history WHERE created_at < ?";
        return executeUpdate(sql, "DELETE OLD HISTORY", date) != 0;
    }

    @Override
    public String findLatestSnapshotBySurveyId(Integer surveyId) {
        String sql = "SELECT snapshot FROM history WHERE id_survey = ? ORDER BY created_at DESC LIMIT 1";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getString("snapshot") : null,
                null, "FIND LATEST SNAPSHOT BY SURVEY", surveyId);
    }

    @Override
    public List<String> findAllSnapshotsBySurveyId(Integer surveyId) {
        String sql = "SELECT snapshot FROM history WHERE id_survey = ? ORDER BY created_at DESC";
        return executeQueryWithParams(sql, rs -> {
            List<String> snapshots = new java.util.ArrayList<>();
            while (rs.next()) {
                snapshots.add(rs.getString("snapshot"));
            }
            return snapshots;
        }, List.of(), "FIND ALL SNAPSHOTS BY SURVEY", surveyId);
    }

    @Override
    public List<History> findByActionIn(List<String> actions) {
        if (actions == null || actions.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(actions.size(), "?"));
        String sql = String.format("SELECT * FROM history WHERE action IN (%s) ORDER BY created_at DESC", placeholders);

        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND HISTORY BY ACTIONS", actions.toArray());
    }

    @Override
    public List<History> findRecentActivity(int days) {
        String sql = "SELECT * FROM history WHERE created_at >= NOW() - INTERVAL '? days' ORDER BY created_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND RECENT ACTIVITY", days);
    }
}