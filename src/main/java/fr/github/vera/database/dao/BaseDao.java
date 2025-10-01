package fr.github.vera.database.dao;

import fr.github.vera.database.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseDao {
    protected static final Logger logger = LogManager.getLogger(BaseDao.class);

    private final DatabaseManager databaseManager = DatabaseManager.getInstance();

    protected BaseDao() {
    }

    private void logMetrics(String sql, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        if (executionTime > 100) {
            logger.warn("Slow SQL Query detected: {} executed in {}ms", sql, executionTime);
        } else {
            logger.debug("SQL Query executed in {}ms: {}", executionTime, sql);
        }
    }

    protected <T> T executeQuery(String sql, ResultSetMapper<T> mapper, T defaultValue, String context) {
        long startTime = System.currentTimeMillis();
        try {
            return databaseManager.executeWithConnection(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    T result = mapper.map(rs);
                    logMetrics(sql, startTime);
                    return result;

                } catch (SQLException e) {
                    logger.error("Erreur SQL [{}]: {}", sql, e.getMessage(), e);
                    return defaultValue;
                }
            }, context);

        } catch (Exception e) {
            logger.error("Erreur base de données lors de [{}]: {}", sql, e.getMessage(), e);
            return defaultValue;
        }
    }

    protected <T> T executeQueryWithParams(String sql, ResultSetMapper<T> mapper, T defaultValue, String context, Object... params) {
        long startTime = System.currentTimeMillis();
        try {
            return databaseManager.executeWithConnection(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                    // Paramètres
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        T result = mapper.map(rs);
                        logMetrics(sql, startTime);
                        return result;
                    }
                }
            }, context);

        } catch (Exception e) {
            logger.error("Erreur lors de la requête [{}]: {}", sql, e.getMessage(), e);
            return defaultValue;
        }
    }

    protected int executeUpdate(String sql, String context, Object... params) {
        long startTime = System.currentTimeMillis();
        try {
            return databaseManager.executeWithConnection(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                    // Paramètres
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }

                    int rowsAffected = stmt.executeUpdate();
                    logMetrics(sql, startTime);
                    return rowsAffected;
                }
            }, context);

        } catch (Exception e) {
            logger.error("Erreur lors de la requête [{}]: {}", sql, e.getMessage(), e);
            return 0;
        }
    }

    @FunctionalInterface
    protected interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
