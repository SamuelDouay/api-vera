package fr.github.vera.repository;

import fr.github.vera.model.Survey;

import java.util.List;
import java.util.Optional;

public interface ISurveyRepository extends IRepository<Survey, Integer> {
    List<Survey> getSurveysByUser(Integer userId, int limit, int offset);

    List<Survey> getPublicSurveys(int limit, int offset);

    List<Survey> getActiveSurveys(int limit, int offest);

    List<Survey> getQuizSurveys(int limit, int offset);

    Survey toggleActivation(Integer id);

    Survey toggleVisibility(Integer id);

    Survey duplicateSurvey(Integer id);

    Optional<Survey> getSurveyByToken(String token);

    Survey generateShareToken(Integer id);

    Survey revokeShareToken(Integer id);

    List<Survey> getSurveysByUserAndStatus(Integer userId, boolean isActive, int limit, int offset);

    int countSurveysByUser(Integer userId);

    int countPublicSurveys();

    boolean updateSurveyDescription(Integer id, String description);

    boolean updateSurveyName(Integer id, String name);


}
