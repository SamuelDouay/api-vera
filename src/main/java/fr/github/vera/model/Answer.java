package fr.github.vera.model;

import fr.github.vera.database.Column;
import fr.github.vera.database.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Table(name = "answer")
public class Answer {
    @Column(name = "id", updatable = false)
    private Integer id;

    @Column(name = "id_question")
    @NotNull(message = "Question ID cannot be null")
    private Integer idQuestion;

    @Column(name = "is_anonymous")
    private boolean isAnonymous;

    @Column(name = "original_answer")
    private String originalAnswer;

    @Column(name = "anonymous_answer")
    private String anonymousAnswer;

    @Column(name = "respondent_id")
    @NotBlank(message = "Respondent ID cannot be blank")
    private String respondentId; // Changé de Integer à String

    @Column(name = "is_correct")
    private Boolean isCorrect; // Changé en Boolean pour permettre null

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    public Answer() {
    }

    public Answer(Integer id, Integer idQuestion, boolean isAnonymous, String originalAnswer,
                  String anonymousAnswer, String respondentId, Boolean isCorrect, LocalDateTime submittedAt) {
        this.id = id;
        this.idQuestion = idQuestion;
        this.isAnonymous = isAnonymous;
        this.originalAnswer = originalAnswer;
        this.anonymousAnswer = anonymousAnswer;
        this.respondentId = respondentId;
        this.isCorrect = isCorrect;
        this.submittedAt = submittedAt;
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdQuestion() {
        return idQuestion;
    }

    public void setIdQuestion(Integer idQuestion) {
        this.idQuestion = idQuestion;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public String getOriginalAnswer() {
        return originalAnswer;
    }

    public void setOriginalAnswer(String originalAnswer) {
        this.originalAnswer = originalAnswer;
    }

    public String getAnonymousAnswer() {
        return anonymousAnswer;
    }

    public void setAnonymousAnswer(String anonymousAnswer) {
        this.anonymousAnswer = anonymousAnswer;
    }

    public String getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(String respondentId) {
        this.respondentId = respondentId;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", idQuestion=" + idQuestion +
                ", isAnonymous=" + isAnonymous +
                ", originalAnswer='" + originalAnswer + '\'' +
                ", anonymousAnswer='" + anonymousAnswer + '\'' +
                ", respondentId='" + respondentId + '\'' +
                ", isCorrect=" + isCorrect +
                ", submittedAt=" + submittedAt +
                '}';
    }
}