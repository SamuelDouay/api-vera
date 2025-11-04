package fr.github.vera;

import fr.github.vera.config.JerseyConfig;
import fr.github.vera.database.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final String BASE_URI = "http://localhost:8080/api/";

    public static void main(String[] args) {
        HttpServer server = null;
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        try {
            // 1. Initialiser le pool de connexions
            logger.info("=".repeat(60));
            logger.info("Démarrage de l'application VERA API");
            logger.info("=".repeat(60));

            logger.info("Initialisation de la base de données...");
            databaseManager.initialize();

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
                logger.info("Arrêt du serveur...");
                try {
                    finalServer.shutdown();
                    databaseManager.shutdown();
                    logger.info("Serveur arrêté proprement");
                } catch (Exception e) {
                    logger.error("Erreur lors de l'arrêt", e);
                }
            }));

            // Démarrer le serveur
            server.start();

            logger.info("=".repeat(60));
            logger.info("✓ Serveur démarré avec succès!");
            logger.info("✓ API disponible à: {}", BASE_URI);
            logger.info("✓ Swagger UI: {}swagger-ui/", BASE_URI);
            logger.info("✓ Health check: {}admin/health", BASE_URI);
            logger.info("=".repeat(60));
            logger.info("Appuyez sur ENTRÉE pour arrêter le serveur...");

            // Attendre l'entrée utilisateur
            System.in.read();

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
}