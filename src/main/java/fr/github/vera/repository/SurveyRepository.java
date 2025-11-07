package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.Survey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SurveyRepository extends BaseRepository<Survey, Integer> implements ISurveyRepository {
    public SurveyRepository() {
        super("survey", Survey.class);
    }

    @Override
    public Survey save(Survey entity) {
        if (entity.getId() == null) {
            return create(entity);
        } else {
            return update(entity);
        }
    }

    private Survey create(Survey survey) {
        return null;
    }


    private Survey update(Survey survey) {
        return null;
    }

    @Override
    public boolean delete(Integer integer) {
        return false;
    }

    @Override
    protected Survey mapResultSet(ResultSet rs) throws SQLException {
        return null;
    }

    @Override
    protected List<Survey> mapResultSetList(ResultSet rs) throws SQLException {
        return List.of();
    }
}
