package fr.github.vera.database.repository;

import fr.github.vera.database.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseRequest {
    protected static final Logger logger = LogManager.getLogger(BaseRequest.class);

    private final DatabaseManager databaseManager = DatabaseManager.getInstance();

    protected BaseRequest() {
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

    protected Integer executeUpdateWithGeneratedKeys(String sql, String context, Object... params) {
        try {
            return databaseManager.executeWithConnection(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject(i + 1, params[i]);
                    }

                    int affectedRows = ps.executeUpdate();
                    logger.info("{} - {} row(s) affected", context, affectedRows);

                    if (affectedRows == 0) {
                        throw new SQLException("Creating failed, no rows affected.");
                    }

                    // Récupérer l'ID généré
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            return generatedKeys.getInt(1); // Retourne l'ID généré
                        } else {
                            throw new SQLException("Creating failed, no ID obtained.");
                        }
                    }
                }
            }, context);


        } catch (Exception e) {
            logger.error("Error during {}: {}", context, e.getMessage());
            throw new RuntimeException("Database error during " + context, e);
        }
    }

    @FunctionalInterface
    protected interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
