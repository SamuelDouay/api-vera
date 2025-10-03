package fr.github.vera.security;

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
    private static final String SECRET = "Vahgtjj8PN2cFjtEfxkm6QvIid4acyrGPB+N60dG6Wo5l9Rd7Wjnc+OnIokMoyoWh++YJZKzg1CsE2fabQ+Hlc1JEB09FBua";
    private static final long accessTokenValidity = 3600000; // 1 heure
    private static final long refreshTokenValidity = 604800000; // 7 jours
    private final SecretKey secretKey;

    public JwtService() {
        this.secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateAccessToken(Integer userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
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

    public Integer getUserIdFromToken(String token) {
        return validateToken(token).get("userId", Integer.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            return validateToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}