package fr.github.vera;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.internal.ValidationExceptionMapper;

import java.io.IOException;
import java.net.URI;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final String BASE_URI = "http://0.0.0.0:8080/api/";

    public static void main(String[] args) throws IOException {
        final ResourceConfig config = new ResourceConfig()
                .packages("your.package.resources") // Remplacer par votre package
                .register(UserResource.class)
                .register(HealthResource.class)
                .register(MetricsResource.class)
                .register(JacksonFeature.class)
                .register(ValidationExceptionMapper.class)
                .register(GeneralExceptionMapper.class)
                .property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        // Configuration Grizzly pour performance
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config, false);

        // Optimisation des threads
        NetworkListener listener = server.getListeners().iterator().next();
        TCPNIOTransport transport = listener.getTransport();
        transport.setIOStrategy(WorkerThreadIOStrategy.getInstance());
        transport.setSelectorRunnersCount(Runtime.getRuntime().availableProcessors());
        transport.setWorkerThreadPoolConfig(null); // Use cached thread pool

        server.start();

        LOGGER.info("Jersey app started with WADL available at " + BASE_URI + "application.wadl");
        LOGGER.info("Hit enter to stop it...");
        System.in.read();
        server.shutdownNow();
    }
}