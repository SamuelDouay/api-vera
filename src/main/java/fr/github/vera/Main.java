package fr.github.vera;

import fr.github.vera.config.JerseyConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final String BASE_URI = "http://0.0.0.0:8080/api/";

    public static void main(String[] args) throws IOException {
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI),
                new JerseyConfig()
        );

        LOGGER.info("Jersey app started at: {}", BASE_URI);
        LOGGER.info("Swagger UI available at: {}swagger-ui/", BASE_URI);
        LOGGER.info("Hit enter to stop it...");

        System.in.read();
        server.shutdown();
    }
}