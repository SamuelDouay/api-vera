package fr.github.vera.services;

import fr.github.vera.model.Survey;
import fr.github.vera.repository.ISurveyRepository;
import fr.github.vera.repository.SurveyRepository;

import java.util.List;
import java.util.Optional;

public class SurveyService extends BaseService<Survey, Integer, ISurveyRepository> {

    public SurveyService() {
        super(new SurveyRepository());
    }

    public List<Survey> getSurveysByUser(Integer userId, int limit, int offset) {
        return repository.getSurveysByUser(userId, limit, offset);
    }

    public List<Survey> getPublicSurveys(int limit, int offset) {
        return repository.getPublicSurveys(limit, offset);
    }

    public List<Survey> getActiveSurveys(int limit, int offset) {
        return repository.getActiveSurveys(limit, offset);
    }

    public List<Survey> getQuizSurveys(int limit, int offset) {
        return repository.getQuizSurveys(limit, offset);
    }

    public Survey toggleActivation(Integer id) {
        return repository.toggleActivation(id);
    }

    public Survey toggleVisibility(Integer id) {
        return repository.toggleVisibility(id);
    }

    public Survey duplicateSurvey(Integer id) {
        return repository.duplicateSurvey(id);
    }

    public Optional<Survey> getSurveyByToken(String token) {
        return repository.getSurveyByToken(token);
    }

    public Survey generateShareToken(Integer id) {
        return repository.generateShareToken(id);
    }

    public Survey revokeShareToken(Integer id) {
        return repository.revokeShareToken(id);
    }

    public List<Survey> getSurveysByUserAndStatus(Integer userId, boolean isActive, int limit, int offset) {
        return repository.getSurveysByUserAndStatus(userId, isActive, limit, offset);
    }

    public int countSurveysByUser(Integer userId) {
        return repository.countSurveysByUser(userId);
    }

    public int countPublicSurveys() {
        return repository.countPublicSurveys();
    }

    public boolean updateSurveyDescription(Integer id, String description) {
        return repository.updateSurveyDescription(id, description);
    }

    public boolean updateSurveyName(Integer id, String name) {
        return repository.updateSurveyName(id, name);
    }
}