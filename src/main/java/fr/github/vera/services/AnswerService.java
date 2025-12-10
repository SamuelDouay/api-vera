package fr.github.vera.services;

import fr.github.vera.model.Answer;
import fr.github.vera.repository.AnswerRepository;
import fr.github.vera.repository.IAnswerRepository;

import java.util.List;

public class AnswerService extends BaseService<Answer, Integer, IAnswerRepository> {

    public AnswerService() {
        super(new AnswerRepository());
    }

    // Méthodes spécifiques aux réponses
    public List<Answer> getAnswersByQuestion(Integer questionId) {
        return repository.findByQuestionId(questionId);
    }

    public List<Answer> getAnswersByRespondent(String respondentId) {
        return repository.findByRespondentId(respondentId);
    }

    public List<Answer> getAnswersBySurvey(Integer surveyId) {
        return repository.findBySurveyId(surveyId);
    }

    // Réponses anonymes
    public List<Answer> getAnonymousAnswers(Integer questionId) {
        return repository.findAnonymousAnswers(questionId);
    }

    // Réponses correctes
    public List<Answer> getCorrectAnswers(Integer questionId) {
        return repository.findCorrectAnswers(questionId);
    }

    // Statistiques
    public int countAnswersByQuestion(Integer questionId) {
        return repository.countByQuestionId(questionId);
    }

    public int countAnswersBySurvey(Integer surveyId) {
        return repository.countBySurveyId(surveyId);
    }

    public int countCorrectAnswersByQuestion(Integer questionId) {
        return repository.countCorrectAnswersByQuestionId(questionId);
    }

    public int countAnonymousAnswersByQuestion(Integer questionId) {
        return repository.countAnonymousAnswersByQuestionId(questionId);
    }

    // Analyses
    public Double getQuestionAverageScore(Integer questionId) {
        return repository.getAverageScoreByQuestionId(questionId);
    }

    public Double getSurveyAverageScore(Integer surveyId) {
        return repository.getAverageScoreBySurveyId(surveyId);
    }

    public boolean markAnswerAsCorrect(Integer answerId, boolean isCorrect) {
        return repository.markAnswerAsCorrect(answerId, isCorrect);
    }

    public boolean anonymizeAnswer(Integer answerId) {
        return repository.anonymizeAnswer(answerId);
    }

    // Vérifications
    public boolean hasRespondentAnsweredQuestion(String respondentId, Integer questionId) {
        return repository.hasRespondentAnsweredQuestion(respondentId, questionId);
    }

}