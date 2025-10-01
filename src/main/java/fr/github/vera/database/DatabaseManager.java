package fr.github.vera.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.github.vera.config.ConfigProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final ConfigProperties CONFIG_PROPERTIES = ConfigProperties.getInstance();
    private static final Logger logger = LogManager.getLogger();

    // Singleton instance
    private static volatile DatabaseManager instance;
    private static final Object lock = new Object();

    private HikariDataSource dataSource;
    private boolean initialized = false;
    private boolean initializing = false;

    // Constructeur privé pour empêcher l'instanciation directe
    private DatabaseManager() {
        // no param
    }

    // Méthode pour obtenir l'instance unique
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public synchronized void initialize() {
        if (initialized) {
            logger.warn("DatabaseManager déjà initialisé");
            return;
        }

        if (initializing) {
            logger.warn("DatabaseManager en cours d'initialisation");
            return;
        }

        initializing = true;

        try {
            loadPostgreSQLDriver();
            this.dataSource = createDataSource();
            testConnection();
            initializeDatabaseSchema();
            registerShutdownHook();
            this.initialized = true;
            logger.debug("DatabaseManager initialisé avec succès");
        } finally {
            initializing = false;
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
            config.setMinimumIdle(Integer.parseInt(CONFIG_PROPERTIES.getProperty("db.pool.size")));
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

    private void initializeDatabaseSchema() {
        try {
            // Vérifier si les tables existent déjà
            if (!isDatabaseInitialized()) {
                logger.info("Initialisation du schéma de base de données...");
                executeSqlScript("/database/init-db.sql");
                logger.info("Schéma de base de données initialisé avec succès");
            } else {
                logger.debug("Base de données déjà initialisée");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation du schéma", e);
        }
    }

    public <T> T executeWithConnection(DatabaseAction<T> action, String context) {
        try (Connection conn = getConnection()) {
            return action.execute(conn);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de " + context, e);
        }
    }

    @FunctionalInterface
    public interface DatabaseAction<T> {
        T execute(Connection connection) throws SQLException;
    }

    private boolean isDatabaseInitialized() {
        String checkTableSql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'users')";

        return executeWithConnection(connection -> {
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(checkTableSql)) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        }, "vérification initialisation BDD");
    }

    private void executeSqlScript(String scriptPath) {
        executeWithConnection(connection -> {
            try {
                String script = loadScriptFromResources(scriptPath);
                List<String> sqlCommands = splitSqlScript(script);

                for (String sqlCommand : sqlCommands) {
                    String trimmedSql = sqlCommand.trim();
                    if (!trimmedSql.isEmpty() && !trimmedSql.startsWith("--")) {
                        try (var statement = connection.createStatement()) {
                            statement.execute(trimmedSql);
                            logger.debug("SQL exécuté: {}", trimmedSql.substring(0, Math.min(50, trimmedSql.length())) + "...");
                        }
                    }
                }
                return null;
            } catch (Exception e) {
                throw new SQLException("Erreur lors de l'exécution du script SQL", e);
            }
        }, "exécution script d'initialisation");
    }

    private List<String> splitSqlScript(String script) {
        List<String> commands = new ArrayList<>();
        StringBuilder currentCommand = new StringBuilder();
        boolean inDollarQuote = false;
        String dollarQuoteTag = null;

        String[] lines = script.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Ignorer les commentaires complets
            if (trimmedLine.startsWith("--") && !inDollarQuote) {
                continue;
            }

            // Détecter les dollar quotes
            if (trimmedLine.contains("$$")) {
                if (!inDollarQuote) {
                    inDollarQuote = true;
                    dollarQuoteTag = "$$";
                } else if (trimmedLine.contains(dollarQuoteTag)) {
                    inDollarQuote = false;
                }
            }

            currentCommand.append(line).append("\n");

            // Si on trouve un ; en dehors d'un dollar quote, c'est la fin de la commande
            if (!inDollarQuote && line.contains(";")) {
                commands.add(currentCommand.toString());
                currentCommand = new StringBuilder();
            }
        }

        // Ajouter la dernière commande si elle existe
        if (currentCommand.length() > 0) {
            commands.add(currentCommand.toString());
        }

        return commands;
    }

    private String loadScriptFromResources(String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                throw new FileNotFoundException("Script SQL non trouvé: " + path);
            }
            return new String(Files.readAllBytes(Paths.get(resource.toURI())));
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger le script SQL: " + path, e);
        }
    }
}