package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.Survey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SurveyRepository extends BaseRepository<Survey, Integer> implements ISurveyRepository {
    public SurveyRepository() {
        super("survey", Survey.class);
    }

    @Override
    public List<Survey> getSurveysByUser(Integer userId, int limit, int offset) {
        String sql = "SELECT * FROM survey WHERE id_user = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "GET SURVEYS BY USER", userId, limit, offset);
    }

    @Override
    public List<Survey> getPublicSurveys(int limit, int offset) {
        String sql = "SELECT * FROM survey WHERE is_public = true AND is_active = true ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "GET PUBLIC SURVEYS", limit, offset);
    }

    @Override
    public List<Survey> getActiveSurveys(int limit, int offset) {
        String sql = "SELECT * FROM survey WHERE is_active = true ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "GET ACTIVE SURVEYS", limit, offset);
    }

    @Override
    public List<Survey> getQuizSurveys(int limit, int offset) {
        String sql = "SELECT * FROM survey WHERE is_quiz = true AND is_active = true ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "GET QUIZ SURVEYS", limit, offset);
    }

    @Override
    public Survey toggleActivation(Integer id) {
        String sql = "UPDATE survey SET is_active = NOT is_active WHERE id = ? RETURNING *";
        return executeQueryWithParams(sql, rs -> rs.next() ? mapResultSet(rs) : null, null,
                "TOGGLE SURVEY ACTIVATION", id);
    }

    @Override
    public Survey toggleVisibility(Integer id) {
        String sql = "UPDATE survey SET is_public = NOT is_public WHERE id = ? RETURNING *";
        return executeQueryWithParams(sql, rs -> rs.next() ? mapResultSet(rs) : null, null,
                "TOGGLE SURVEY VISIBILITY", id);
    }

    @Override
    public Survey duplicateSurvey(Integer id) {
        // Récupérer le survey original
        Optional<Survey> originalSurvey = findById(id);
        if (originalSurvey.isEmpty()) {
            return null;
        }

        Survey original = originalSurvey.get();

        // Créer une copie avec un nouveau nom et sans token de partage
        String sql = """
                INSERT INTO survey (name, anonymization, description, id_user, is_quiz, is_active, allow_editing, is_public, share_token)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING *
                """;

        String newName = original.getName() + " (Copie)";

        return executeQueryWithParams(sql, rs -> rs.next() ? mapResultSet(rs) : null, null,
                "DUPLICATE SURVEY",
                newName, original.getAnonymization(), original.getDescription(),
                original.getUserId(), original.isQuiz(), original.isActive(),
                original.isEditing(), original.isPublic(), null);
    }

    @Override
    public Optional<Survey> getSurveyByToken(String token) {
        String sql = "SELECT * FROM survey WHERE share_token = ? AND is_active = true";
        return executeQueryWithParams(sql, rs -> rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty(),
                Optional.empty(), "GET SURVEY BY TOKEN", token);
    }

    @Override
    public Survey generateShareToken(Integer id) {
        String newToken = UUID.randomUUID().toString();
        String sql = "UPDATE survey SET share_token = ? WHERE id = ? RETURNING *";
        return executeQueryWithParams(sql, rs -> rs.next() ? mapResultSet(rs) : null, null,
                "GENERATE SHARE TOKEN", newToken, id);
    }

    @Override
    public Survey revokeShareToken(Integer id) {
        String sql = "UPDATE survey SET share_token = NULL WHERE id = ? RETURNING *";
        return executeQueryWithParams(sql, rs -> rs.next() ? mapResultSet(rs) : null, null,
                "REVOKE SHARE TOKEN", id);
    }

    // Méthodes supplémentaires utiles

    @Override
    public List<Survey> getSurveysByUserAndStatus(Integer userId, boolean isActive, int limit, int offset) {
        String sql = "SELECT * FROM survey WHERE id_user = ? AND is_active = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "GET SURVEYS BY USER AND STATUS", userId, isActive, limit, offset);
    }

    @Override
    public int countSurveysByUser(Integer userId) {
        String sql = "SELECT COUNT(*) FROM survey WHERE id_user = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT SURVEYS BY USER", userId);
    }

    @Override
    public int countPublicSurveys() {
        String sql = "SELECT COUNT(*) FROM survey WHERE is_public = true AND is_active = true";
        return executeQuery(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0, "COUNT PUBLIC SURVEYS");
    }

    @Override
    public boolean updateSurveyDescription(Integer id, String description) {
        String sql = "UPDATE survey SET description = ? WHERE id = ?";
        return executeUpdate(sql, "UPDATE SURVEY DESCRIPTION", description, id) != 0;
    }

    @Override
    public boolean updateSurveyName(Integer id, String name) {
        String sql = "UPDATE survey SET name = ? WHERE id = ?";
        return executeUpdate(sql, "UPDATE SURVEY NAME", name, id) != 0;
    }
}