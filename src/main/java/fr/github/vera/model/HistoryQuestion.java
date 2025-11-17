package fr.github.vera.model;

import fr.github.vera.database.Column;
import fr.github.vera.database.Table;
import jakarta.validation.constraints.NotNull;

@Table(name = "history_question")
public class HistoryQuestion {
    @Column(name = "id", updatable = false)
    private Integer id;

    @Column(name = "id_question")
    @NotNull(message = "Question ID cannot be null")
    private Integer idQuestion;

    @Column(name = "id_survey")
    @NotNull(message = "Survey ID cannot be null")
    private Integer idSurvey;

    @Column(name = "action")
    @NotNull(message = "Action cannot be null")
    private String action;

    @Column(name = "snapshot")
    @NotNull(message = "Snapshot cannot be null")
    private String snapshot;

    @Column(name = "id_user")
    @NotNull(message = "User ID cannot be null")
    private Integer idUser;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    public HistoryQuestion() {
    }

    public HistoryQuestion(Integer id, Integer idQuestion, Integer idSurvey, String action,
                           String snapshot, Integer idUser, java.time.LocalDateTime createdAt) {
        this.id = id;
        this.idQuestion = idQuestion;
        this.idSurvey = idSurvey;
        this.action = action;
        this.snapshot = snapshot;
        this.idUser = idUser;
        this.createdAt = createdAt;
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

    public Integer getIdSurvey() {
        return idSurvey;
    }

    public void setIdSurvey(Integer idSurvey) {
        this.idSurvey = idSurvey;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "HistoryQuestion{" +
                "id=" + id +
                ", idQuestion=" + idQuestion +
                ", idSurvey=" + idSurvey +
                ", action='" + action + '\'' +
                ", snapshot='" + snapshot + '\'' +
                ", idUser=" + idUser +
                ", createdAt=" + createdAt +
                '}';
    }
}