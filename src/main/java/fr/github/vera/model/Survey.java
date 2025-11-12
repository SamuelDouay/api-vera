package fr.github.vera.model;

import fr.github.vera.database.Column;
import fr.github.vera.database.Table;

@Table(name = "survey")
public class Survey {
    @Column(name = "id", updatable = false)
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "anonymization")
    private String anonymization;
    @Column(name = "description")
    private String description;
    @Column(name = "id_user")
    private Integer userId;
    @Column(name = "is_quiz")
    private boolean quiz;
    @Column(name = "is_active")
    private boolean active;
    @Column(name = "allow_editing")
    private boolean editing;
    @Column(name = "is_public")
    private boolean isPublic;
    @Column(name = "share_token")
    private String shareToken;

    public Survey() {

    }

    public Survey(Integer id, String name, String anonymization, String description, Integer userId, boolean quiz, boolean active, boolean editing, boolean isPublic, String shareToken) {
        this.id = id;
        this.name = name;
        this.anonymization = anonymization;
        this.description = description;
        this.userId = userId;
        this.quiz = quiz;
        this.active = active;
        this.editing = editing;
        this.isPublic = isPublic;
        this.shareToken = shareToken;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnonymization() {
        return anonymization;
    }

    public void setAnonymization(String anonymization) {
        this.anonymization = anonymization;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean isQuiz() {
        return quiz;
    }

    public void setQuiz(boolean quiz) {
        this.quiz = quiz;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", anonymization='" + anonymization + '\'' +
                ", description='" + description + '\'' +
                ", userId=" + userId +
                ", quiz=" + quiz +
                ", active=" + active +
                ", editing=" + editing +
                ", isPublic=" + isPublic +
                ", shareToken=" + shareToken +
                '}';
    }
}
