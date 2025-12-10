package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.BlacklistedToken;

import java.sql.ResultSet;
import java.sql.Timestamp;

public class BlacklistedTokenRepository extends BaseRepository<BlacklistedToken, String> {
    public BlacklistedTokenRepository() {
        super("blacklisted_tokens", BlacklistedToken.class);
    }

    @Override
    public BlacklistedToken save(BlacklistedToken token) {
        String sql = "INSERT INTO blacklisted_tokens (token, expires_at, user_id, reason) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (token) DO UPDATE SET " +
                "expires_at = EXCLUDED.expires_at, " +
                "user_id = EXCLUDED.user_id, " +
                "reason = EXCLUDED.reason";

        executeUpdate(sql, "SAVE BLACKLISTED TOKEN",
                token.getId(),
                Timestamp.from(token.getExpiresAt()),
                token.getIdUser(),
                token.getReason());
        return token;
    }

    @Override
    public boolean delete(String token) {
        String sql = "DELETE FROM blacklisted_tokens WHERE token = ?";
        return executeUpdate(sql, "DELETE BLACKLISTED TOKEN", token) != 0;
    }

    public int cleanupExpiredTokens() {
        String sql = "DELETE FROM blacklisted_tokens WHERE expires_at < NOW()";
        return executeUpdate(sql, "CLEANUP EXPIRED TOKENS");
    }

    public boolean existsByToken(String token) {
        String sql = "SELECT 1 FROM blacklisted_tokens WHERE token = ? AND expires_at > NOW() LIMIT 1";
        return executeQueryWithParams(sql,
                ResultSet::next,
                false,
                "CHECK TOKEN EXISTS",
                token);
    }
}
