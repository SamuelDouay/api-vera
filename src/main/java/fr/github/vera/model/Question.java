package fr.github.vera.model;

import fr.github.vera.database.Column;
import fr.github.vera.database.Table;
import jakarta.validation.constraints.NotBlank;

@Table(name = "question")
public class Question {
    @Column(name = "id", updatable = false)
    private Integer id;
    @Column(name = "title")
    @NotBlank(message = "title cannot be blank")
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name = "id_survey")
    private Integer surveyId;
    @Column(name = "is_mandatory")
    private boolean mandatory;
    @Column(name = "correct_answer")
    private String correctAnswer;
    @Column(name = "display_order")
    private Integer displayOrder;

    public Question() {

    }

    public Question(Integer displayOrder, String correctAnswer, boolean mandatory, Integer surveyId, String description, String title, Integer id) {
        this.displayOrder = displayOrder;
        this.correctAnswer = correctAnswer;
        this.mandatory = mandatory;
        this.surveyId = surveyId;
        this.description = description;
        this.title = title;
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Integer surveyId) {
        this.surveyId = surveyId;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", surveyId=" + surveyId +
                ", mandatory=" + mandatory +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", displayOrder=" + displayOrder +
                '}';
    }
}
