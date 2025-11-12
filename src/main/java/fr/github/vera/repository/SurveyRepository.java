package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.Survey;

public class SurveyRepository extends BaseRepository<Survey, Integer> implements ISurveyRepository {
    public SurveyRepository() {
        super("survey", Survey.class);
    }
}
