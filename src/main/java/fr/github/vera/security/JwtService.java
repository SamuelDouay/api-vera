package fr.github.vera.security;

import fr.github.vera.config.ConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtService {
    private static final String SECRET = ConfigProperties.getInstance().getProperty("secret");
    private static final long ACCESS_TOKEN_VALIDITY = 3600000; // 1 heure
    private static final long REFRESH_TOKEN_VALIDITY = 604800000; // 7 jours
    private final SecretKey secretKey;

    public JwtService() {
        this.secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateAccessToken(Integer userId, String email, boolean admin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("isAdmin", admin);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Date getExpiration(String token) {
        try {
            Claims claims = this.validateToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            return new Date(System.currentTimeMillis() + 3600000);
        }
    }

    public Integer getUserId(String token) {
        try {
            Claims claims = this.validateToken(token);
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expiré");
        } catch (JwtException e) {
            throw new RuntimeException("Token invalide");
        }
    }

    public String getEmailFromToken(String token) {
        return validateToken(token).getSubject();
    }

    public Integer extractUserIdFromToken(String token) {
        try {
            Claims claims = new JwtService().validateToken(token);
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'extraire l'userId du token");
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return validateToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}