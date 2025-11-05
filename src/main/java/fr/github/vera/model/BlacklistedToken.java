package fr.github.vera.model;

import java.time.Instant;

public class BlacklistedToken {
    private String token;
    private Instant expiresAt;
    private Instant createdAt;
    private Integer userId;
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
}
