package fr.github.vera.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.github.vera.config.ConfigProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    public static final ConfigProperties CONFIG_PROPERTIES = ConfigProperties.getInstance();
    private static final Logger logger = LogManager.getLogger();
    private HikariDataSource dataSource;
    private boolean initialized = false;
    private boolean initializing = false;

    public DatabaseManager() {
        // no param
    }

    public synchronized void initialize() {
        if (initialized) {
            logger.warn("DatabaseManager déjà initialisé");
            return;
        }
        synchronized (DatabaseManager.class) {
            if (initialized) return;
            initializing = true;

            try {
                loadPostgreSQLDriver();
                this.dataSource = createDataSource();
                testConnection();
                registerShutdownHook();
                this.initialized = true;
                logger.debug("DatabaseManager initialisé avec succès");
            } finally {
                initializing = false;
            }
        }
    }

    private void loadPostgreSQLDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            logger.debug("Driver PostgreSQL chargé avec succès");
        } catch (ClassNotFoundException e) {
            logger.error("Driver PostgreSQL non trouvé dans le classpath");
        }
    }

    private HikariDataSource createDataSource() {
        try {
            HikariConfig config = new HikariConfig();

            // Configuration PostgreSQL depuis les propriétés
            config.setJdbcUrl(CONFIG_PROPERTIES.getProperty("db.url"));
            config.setUsername(CONFIG_PROPERTIES.getProperty("db.username"));
            config.setPassword(CONFIG_PROPERTIES.getProperty("db.password"));
            config.setDriverClassName("org.postgresql.Driver");

            // Configuration du pool de connexions
            config.setMaximumPoolSize(Integer.parseInt(CONFIG_PROPERTIES.getProperty("db.pool.size")));
            config.setMinimumIdle(Integer.parseInt(CONFIG_PROPERTIES.getProperty("db.pool.size"))); // Même valeur que le pool max pour PostgreSQL
            config.setConnectionTimeout(Long.parseLong(CONFIG_PROPERTIES.getProperty("db.connection.timeout")));
            config.setIdleTimeout(Long.parseLong(CONFIG_PROPERTIES.getProperty("db.idle.timeout")));
            config.setMaxLifetime(Long.parseLong(CONFIG_PROPERTIES.getProperty("db.max.lifetime")));
            config.setPoolName("PostgreSQL-HikariCP-Pool");

            // Optimisations spécifiques à PostgreSQL
            config.addDataSourceProperty("ApplicationName", "api-vera");
            config.addDataSourceProperty("tcpKeepAlive", "true");
            config.addDataSourceProperty("socketTimeout", "30");
            config.addDataSourceProperty("connectTimeout", "10");

            // Performance tuning
            config.addDataSourceProperty("preparedStatementCacheQueries", "256");
            config.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
            config.addDataSourceProperty("defaultRowFetchSize", "100");

            // Encodage
            config.addDataSourceProperty("characterEncoding", "UTF-8");

            // Validation des connexions
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);

            logger.debug("Configuration HikariCP créée pour PostgreSQL");
            return new HikariDataSource(config);

        } catch (Exception e) {
            logger.error("Erreur lors de la création du DataSource: {}", e.getMessage(), e);
        }
        return null;
    }

    private void testConnection() {
        try (Connection testConnection = getConnection()) {
            if (!testConnection.isValid(5)) {
                logger.warn("Validation de connexion échouée - connexion invalide");
            }
            logger.debug("Test de connexion PostgreSQL réussi");

        } catch (SQLException e) {
            logger.error("Test de connexion PostgreSQL échoué: {}", e.getMessage(), e);
            logger.warn("test de connexion PostgreSQL");
        } catch (Exception e) {
            logger.error("Erreur inattendue lors du test de connexion PostgreSQL: {}", e.getMessage(), e);
            logger.warn("Erreur inattendue lors du test de connexion PostgreSQL", e);
        }
    }

    public Connection getConnection() {
        if ((!initialized && !initializing) || dataSource == null) {
            logger.warn("DatabaseManager non initialisé - appelez initialize() d'abord");
        }

        if (dataSource.isClosed()) {
            logger.warn("DataSource fermé - impossible d'obtenir une connexion");
        }

        try {
            Connection connection = dataSource.getConnection();
            if (connection == null) {
                logger.warn("Le pool a retourné une connexion null");
            }

            // Optionnel : configurer la connexion PostgreSQL
            connection.setAutoCommit(true);

            return connection;

        } catch (SQLException e) {
            logger.error("Erreur lors de l'obtention d'une connexion PostgreSQL: {}", e.getMessage(), e);

            // Vérifier si c'est un problème de pool épuisé
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                logger.warn("Timeout lors de l'obtention d'une connexion - pool épuisé", e);
            }
        }
        return null;
    }

    public String getPoolStats() {
        if (dataSource != null && !dataSource.isClosed()) {
            return String.format("PostgreSQL Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool PostgreSQL non disponible";
    }

    public synchronized void shutdown() {
        cleanup();
    }

    private void cleanup() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                logger.debug("Fermeture du pool de connexions PostgreSQL: {}", getPoolStats());
                dataSource.close();
                logger.debug("Pool de connexions PostgreSQL fermé avec succès");
            } catch (Exception e) {
                logger.warn("Erreur lors de la fermeture du DataSource PostgreSQL: {}", e.getMessage());
            }
        }
        initialized = false;
        dataSource = null;
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.debug("Shutdown hook - Fermeture du DatabaseManager PostgreSQL");
            shutdown();
        }));
    }
}