package fr.github.vera;

import fr.github.vera.config.JerseyConfig;
import fr.github.vera.database.DatabaseManager;
import fr.github.vera.repository.BlacklistedTokenRepository;
import fr.github.vera.services.TokenBlacklistService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final String BASE_URI = "http://0.0.0.0:8080/";
    private static TokenBlacklistService tokenBlacklistService;

    public static void main(String[] args) {
        HttpServer server = null;
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        final Object sync = new Object();

        try {
            // 1. Initialiser le pool de connexions
            logger.info("=".repeat(60));
            logger.info("Démarrage de l'application VERA API");
            logger.info("=".repeat(60));

            logger.info("Initialisation de la base de données...");
            databaseManager.initialize();

            logger.info("Initialisation du service de purge des tokens...");
            initializeTokenPurgeService();

            // 3. Démarrer le serveur Jersey
            logger.info("Démarrage du serveur HTTP...");
            server = GrizzlyHttpServerFactory.createHttpServer(
                    URI.create(BASE_URI),
                    new JerseyConfig(),
                    false // Ne pas démarrer automatiquement
            );

            // Configurer le shutdown hook
            final HttpServer finalServer = server;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdownApplication(finalServer, databaseManager);
            }));

            // Démarrer le serveur
            server.start();

            logger.info("=".repeat(60));
            logger.info("✓ Serveur démarré avec succès!");
            logger.info("✓ API disponible à: {}", BASE_URI);
            logger.info("✓ Swagger UI: {}api/swagger-ui/", BASE_URI);
            logger.info("✓ Health check: {}api/admin/health", BASE_URI);
            logger.info("=".repeat(60));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Signal d'arrêt reçu...");
                synchronized (sync) {
                    sync.notifyAll();
                }
            }));

            synchronized (sync) {
                sync.wait();
            }

        } catch (Exception e) {
            logger.error("Erreur fatale lors du démarrage", e);
            System.exit(1);
        } finally {
            if (server != null) {
                server.shutdown();
            }
            databaseManager.shutdown();
        }
    }

    private static void initializeTokenPurgeService() {
        try {
            BlacklistedTokenRepository blacklistRepository = new BlacklistedTokenRepository();
            tokenBlacklistService = new TokenBlacklistService(blacklistRepository);

            logger.info("✓ Service de purge des tokens initialisé");
            logger.info("✓ Purge automatique toutes les 60 minutes");

        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du service de purge", e);
            throw new RuntimeException("Impossible de démarrer le service de purge", e);
        }
    }

    private static void shutdownApplication(HttpServer server, DatabaseManager databaseManager) {
        logger.info("Arrêt de l'application...");

        try {
            // 1. Arrêter le service de purge
            if (tokenBlacklistService != null) {
                logger.info("Arrêt du service de purge...");
                tokenBlacklistService.shutdown();
                logger.info("✓ Service de purge arrêté");
            }

            // 2. Arrêter le serveur
            if (server != null) {
                server.shutdown();
                logger.info("✓ Serveur HTTP arrêté");
            }

            // 3. Fermer la base de données
            databaseManager.shutdown();
            logger.info("✓ Connexions base de données fermées");

            logger.info("✓ Application arrêtée proprement");

        } catch (Exception e) {
            logger.error("Erreur lors de l'arrêt de l'application", e);
        }
    }

    public static TokenBlacklistService getTokenBlacklistService() {
        return tokenBlacklistService;
    }
}