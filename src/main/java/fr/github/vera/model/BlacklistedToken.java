package fr.github.vera.model;

import fr.github.vera.database.Column;
import fr.github.vera.database.Table;

import java.time.Instant;

@Table(name = "blacklisted_tokens")
public class BlacklistedToken implements Identifiable<String> {
    @Column(name = "token")
    private String token;
    @Column(name = "expires_at")
    private Instant expiresAt;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "reason")
    private String reason;

    public BlacklistedToken() {

    }

    public BlacklistedToken(String token, Instant expiresAt, Integer userId, String reason) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.userId = userId;
        this.reason = reason;
        this.createdAt = Instant.now();
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreateAt() {
        return createdAt;
    }

    public void setCreateAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getIdUser() {
        return userId;
    }

    public void setIdUser(Integer userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public void setId(String id) {

    }
}
