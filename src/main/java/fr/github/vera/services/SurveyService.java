package fr.github.vera.services;

import fr.github.vera.model.Survey;
import fr.github.vera.repository.ISurveyRepository;
import fr.github.vera.repository.SurveyRepository;

public class SurveyService extends BaseService<Survey, Integer, ISurveyRepository> {

    public SurveyService() {
        super(new SurveyRepository());
    }
}
