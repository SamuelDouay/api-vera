package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.Answer;

import java.util.List;
import java.util.Optional;

public class AnswerRepository extends BaseRepository<Answer, Integer> implements IAnswerRepository {

    public AnswerRepository() {
        super("answer", Answer.class);
    }

    @Override
    public List<Answer> findByQuestionId(Integer questionId) {
        String sql = "SELECT * FROM answer WHERE id_question = ? ORDER BY submitted_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND ANSWERS BY QUESTION ID", questionId);
    }

    @Override
    public List<Answer> findByRespondentId(String respondentId) {
        String sql = "SELECT * FROM answer WHERE respondent_id = ? ORDER BY submitted_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND ANSWERS BY RESPONDENT ID", respondentId);
    }

    @Override
    public List<Answer> findByQuestionIdAndRespondentId(Integer questionId, String respondentId) {
        String sql = "SELECT * FROM answer WHERE id_question = ? AND respondent_id = ? ORDER BY submitted_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND ANSWERS BY QUESTION AND RESPONDENT", questionId, respondentId);
    }

    @Override
    public List<Answer> findBySurveyId(Integer surveyId) {
        String sql = """
                SELECT a.* FROM answer a 
                JOIN question q ON a.id_question = q.id 
                WHERE q.id_survey = ? 
                ORDER BY a.submitted_at DESC
                """;
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND ANSWERS BY SURVEY ID", surveyId);
    }

    @Override
    public List<Answer> findAnonymousAnswers(Integer questionId) {
        String sql = "SELECT * FROM answer WHERE id_question = ? AND is_anonymous = true ORDER BY submitted_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND ANONYMOUS ANSWERS", questionId);
    }

    @Override
    public List<Answer> findNonAnonymousAnswers(Integer questionId) {
        String sql = "SELECT * FROM answer WHERE id_question = ? AND is_anonymous = false ORDER BY submitted_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND NON ANONYMOUS ANSWERS", questionId);
    }

    @Override
    public List<Answer> findAnswersByAnonymity(Integer questionId, boolean isAnonymous) {
        String sql = "SELECT * FROM answer WHERE id_question = ? AND is_anonymous = ? ORDER BY submitted_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND ANSWERS BY ANONYMITY", questionId, isAnonymous);
    }

    @Override
    public List<Answer> findCorrectAnswers(Integer questionId) {
        String sql = "SELECT * FROM answer WHERE id_question = ? AND is_correct = true ORDER BY submitted_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND CORRECT ANSWERS", questionId);
    }

    @Override
    public List<Answer> findIncorrectAnswers(Integer questionId) {
        String sql = "SELECT * FROM answer WHERE id_question = ? AND is_correct = false ORDER BY submitted_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND INCORRECT ANSWERS", questionId);
    }

    @Override
    public List<Answer> findAnswersByCorrectness(Integer questionId, boolean isCorrect) {
        String sql = "SELECT * FROM answer WHERE id_question = ? AND is_correct = ? ORDER BY submitted_at DESC";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND ANSWERS BY CORRECTNESS", questionId, isCorrect);
    }

    @Override
    public int countByQuestionId(Integer questionId) {
        String sql = "SELECT COUNT(*) FROM answer WHERE id_question = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT ANSWERS BY QUESTION", questionId);
    }

    @Override
    public int countBySurveyId(Integer surveyId) {
        String sql = """
                SELECT COUNT(*) FROM answer a 
                JOIN question q ON a.id_question = q.id 
                WHERE q.id_survey = ?
                """;
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT ANSWERS BY SURVEY", surveyId);
    }

    @Override
    public int countCorrectAnswersByQuestionId(Integer questionId) {
        String sql = "SELECT COUNT(*) FROM answer WHERE id_question = ? AND is_correct = true";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT CORRECT ANSWERS", questionId);
    }

    @Override
    public int countAnonymousAnswersByQuestionId(Integer questionId) {
        String sql = "SELECT COUNT(*) FROM answer WHERE id_question = ? AND is_anonymous = true";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT ANONYMOUS ANSWERS", questionId);
    }

    @Override
    public int countByRespondentId(String respondentId) {
        String sql = "SELECT COUNT(*) FROM answer WHERE respondent_id = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "COUNT ANSWERS BY RESPONDENT", respondentId);
    }

    @Override
    public Double getAverageScoreByQuestionId(Integer questionId) {
        String sql = """
                SELECT AVG(CASE WHEN is_correct THEN 1 ELSE 0 END) 
                FROM answer WHERE id_question = ? AND is_correct IS NOT NULL
                """;
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getDouble(1) : 0.0, 0.0,
                "GET AVERAGE SCORE BY QUESTION", questionId);
    }

    @Override
    public Double getAverageScoreBySurveyId(Integer surveyId) {
        String sql = """
                SELECT AVG(CASE WHEN a.is_correct THEN 1 ELSE 0 END) 
                FROM answer a 
                JOIN question q ON a.id_question = q.id 
                WHERE q.id_survey = ? AND a.is_correct IS NOT NULL
                """;
        return executeQueryWithParams(sql, rs -> rs.next() ? rs.getDouble(1) : 0.0, 0.0,
                "GET AVERAGE SCORE BY SURVEY", surveyId);
    }

    @Override
    public List<Object[]> getAnswerDistribution(Integer questionId) {
        String sql = """
                SELECT original_answer, COUNT(*) as count 
                FROM answer 
                WHERE id_question = ? AND original_answer IS NOT NULL 
                GROUP BY original_answer 
                ORDER BY count DESC
                """;
        return executeQueryWithParams(sql, rs -> {
            List<Object[]> distribution = new java.util.ArrayList<>();
            while (rs.next()) {
                distribution.add(new Object[]{
                        rs.getString("original_answer"),
                        rs.getInt("count")
                });
            }
            return distribution;
        }, List.of(), "GET ANSWER DISTRIBUTION", questionId);
    }

    @Override
    public boolean updateAnswerContent(Integer answerId, String originalAnswer, String anonymousAnswer) {
        String sql = "UPDATE answer SET original_answer = ?, anonymous_answer = ? WHERE id = ?";
        return executeUpdate(sql, "UPDATE ANSWER CONTENT", originalAnswer, anonymousAnswer, answerId) != 0;
    }

    @Override
    public boolean markAnswerAsCorrect(Integer answerId, boolean isCorrect) {
        String sql = "UPDATE answer SET is_correct = ? WHERE id = ?";
        return executeUpdate(sql, "MARK ANSWER AS CORRECT", isCorrect, answerId) != 0;
    }

    @Override
    public boolean anonymizeAnswer(Integer answerId) {
        String sql = "UPDATE answer SET is_anonymous = true, anonymous_answer = original_answer, original_answer = NULL WHERE id = ?";
        return executeUpdate(sql, "ANONYMIZE ANSWER", answerId) != 0;
    }

    @Override
    public boolean deleteByQuestionId(Integer questionId) {
        String sql = "DELETE FROM answer WHERE id_question = ?";
        return executeUpdate(sql, "DELETE ANSWERS BY QUESTION", questionId) != 0;
    }

    @Override
    public boolean deleteByRespondentId(String respondentId) {
        String sql = "DELETE FROM answer WHERE respondent_id = ?";
        return executeUpdate(sql, "DELETE ANSWERS BY RESPONDENT", respondentId) != 0;
    }

    @Override
    public boolean deleteBySurveyId(Integer surveyId) {
        String sql = """
                DELETE FROM answer 
                WHERE id_question IN (SELECT id FROM question WHERE id_survey = ?)
                """;
        return executeUpdate(sql, "DELETE ANSWERS BY SURVEY", surveyId) != 0;
    }

    @Override
    public boolean hasRespondentAnsweredQuestion(String respondentId, Integer questionId) {
        String sql = "SELECT COUNT(*) FROM answer WHERE respondent_id = ? AND id_question = ?";
        int count = executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "CHECK RESPONDENT ANSWERED QUESTION", respondentId, questionId);
        return count > 0;
    }

    @Override
    public Optional<Answer> findLatestAnswerByQuestionAndRespondent(Integer questionId, String respondentId) {
        String sql = """
                SELECT * FROM answer 
                WHERE id_question = ? AND respondent_id = ? 
                ORDER BY submitted_at DESC 
                LIMIT 1
                """;
        return executeQueryWithParams(sql, rs -> rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty(),
                Optional.empty(), "FIND LATEST ANSWER", questionId, respondentId);
    }
}