package fr.github.vera.services;

import fr.github.vera.model.Survey;
import fr.github.vera.repository.ISurveyRepository;
import fr.github.vera.repository.SurveyRepository;

import java.util.List;
import java.util.Optional;

public class SurveyService {
    private final ISurveyRepository surveyRepository;

    public SurveyService() {
        this.surveyRepository = new SurveyRepository();
    }

    public List<Survey> getAllSurvey(int limit, int offset) {
        return surveyRepository.findAll(limit, offset);
    }

    public Optional<Survey> getSurveyById(Integer id) {
        return surveyRepository.findById(id);
    }

    public Survey createSurvey(Survey Survey) {
        return surveyRepository.save(Survey);
    }

    public Survey updateSurvey(Integer id, Survey Survey) {
        Survey.setId(id);
        return surveyRepository.save(Survey);
    }

    public boolean deleteSurvey(Integer id) {
        return surveyRepository.delete(id);
    }

    public int count() {
        return surveyRepository.count();
    }
}
