package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.Question;

import java.util.List;
import java.util.Optional;

public class QuestionRepository extends BaseRepository<Question, Integer> implements IQuestionRepository {

    public QuestionRepository() {
        super("question", Question.class);
    }

    @Override
    public List<Question> findBySurveyId(Integer surveyId) {
        String sql = "SELECT * FROM question WHERE id_survey = ? ORDER BY display_order, created_at";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND QUESTIONS BY SURVEY ID", surveyId);
    }

    @Override
    public List<Question> findBySurveyIdOrdered(Integer surveyId) {
        String sql = "SELECT * FROM question WHERE id_survey = ? ORDER BY display_order ASC, created_at ASC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND QUESTIONS BY SURVEY ID ORDERED", surveyId);
    }

    @Override
    public List<Question> findMandatoryQuestions(Integer surveyId) {
        String sql = "SELECT * FROM question WHERE id_survey = ? AND is_mandatory = true ORDER BY display_order";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND MANDATORY QUESTIONS", surveyId);
    }

    @Override
    public List<Question> findQuestionsBySurveyAndMandatory(Integer surveyId, boolean isMandatory) {
        String sql = "SELECT * FROM question WHERE id_survey = ? AND is_mandatory = ? ORDER BY display_order";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND QUESTIONS BY SURVEY AND MANDATORY", surveyId, isMandatory);
    }

    @Override
    public boolean updateDisplayOrder(Integer questionId, Integer displayOrder) {
        String sql = "UPDATE question SET display_order = ? WHERE id = ?";
        return executeUpdate(sql, "UPDATE QUESTION DISPLAY ORDER", displayOrder, questionId) != 0;
    }

    @Override
    public boolean reorderQuestions(Integer surveyId, List<Integer> questionIdsInOrder) {
        // Cette méthode met à jour l'ordre d'affichage pour plusieurs questions
        String sql = "UPDATE question SET display_order = ? WHERE id = ? AND id_survey = ?";

        try {
            for (int i = 0; i < questionIdsInOrder.size(); i++) {
                Integer questionId = questionIdsInOrder.get(i);
                executeUpdate(sql, "REORDER QUESTIONS", i, questionId, surveyId);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Integer getMaxDisplayOrder(Integer surveyId) {
        String sql = "SELECT COALESCE(MAX(display_order), 0) FROM question WHERE id_survey = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "GET MAX DISPLAY ORDER", surveyId);
    }

    @Override
    public List<Question> findQuizQuestions(Integer surveyId) {
        String sql = "SELECT * FROM question WHERE id_survey = ? AND correct_answer IS NOT NULL ORDER BY display_order";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND QUIZ QUESTIONS", surveyId);
    }

    @Override
    public Optional<Question> findByIdWithCorrectAnswer(Integer questionId) {
        String sql = "SELECT * FROM question WHERE id = ? AND correct_answer IS NOT NULL";
        return executeQueryWithParams(sql, rs -> rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty(),
                Optional.empty(), "FIND QUESTION WITH CORRECT ANSWER", questionId);
    }

    @Override
    public int countBySurveyId(Integer surveyId) {
        String sql = "SELECT COUNT(*) FROM question WHERE id_survey = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT QUESTIONS BY SURVEY", surveyId);
    }

    @Override
    public int countMandatoryBySurveyId(Integer surveyId) {
        String sql = "SELECT COUNT(*) FROM question WHERE id_survey = ? AND is_mandatory = true";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT MANDATORY QUESTIONS", surveyId);
    }

    @Override
    public boolean updateQuestionTitle(Integer questionId, String title) {
        String sql = "UPDATE question SET title = ? WHERE id = ?";
        return executeUpdate(sql, "UPDATE QUESTION TITLE", title, questionId) != 0;
    }

    @Override
    public boolean updateQuestionDescription(Integer questionId, String description) {
        String sql = "UPDATE question SET description = ? WHERE id = ?";
        return executeUpdate(sql, "UPDATE QUESTION DESCRIPTION", description, questionId) != 0;
    }

    @Override
    public boolean toggleMandatoryStatus(Integer questionId) {
        String sql = "UPDATE question SET is_mandatory = NOT is_mandatory WHERE id = ?";
        return executeUpdate(sql, "TOGGLE QUESTION MANDATORY STATUS", questionId) != 0;
    }

    @Override
    public boolean updateCorrectAnswer(Integer questionId, String correctAnswerJson) {
        String sql = "UPDATE question SET correct_answer = ?::json WHERE id = ?";
        return executeUpdate(sql, "UPDATE CORRECT ANSWER", correctAnswerJson, questionId) != 0;
    }

    @Override
    public boolean deleteBySurveyId(Integer surveyId) {
        String sql = "DELETE FROM question WHERE id_survey = ?";
        return executeUpdate(sql, "DELETE QUESTIONS BY SURVEY ID", surveyId) != 0;
    }
}