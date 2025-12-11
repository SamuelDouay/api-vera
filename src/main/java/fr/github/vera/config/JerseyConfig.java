package fr.github.vera.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.github.vera.resources.SwaggerUIResource;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.ext.ContextResolver;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        // Register resources
        packages("fr.github.vera.resources");

        // Register filters and exception mappers
        packages("fr.github.vera.filters");
        packages("fr.github.vera.exception");

        // Configure Swagger
        configureSwagger();

        // Enregistrer Jackson pour JSON
        register(JacksonFeature.class);

        // Configurer Jackson
        register(new ObjectMapperContextResolver());
        // Register Swagger resources
        register(OpenApiResource.class);
        register(SwaggerUIResource.class);
    }

    private void configureSwagger() {
        // Create OpenAPI configuration
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("VERA API")
                        .version("1.0.0")
                        .description("API REST pour l'application VERA"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Entrez votre token JWT sans le mot 'Bearer'"))
                        .addSecuritySchemes("BasicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Authentification basique"))
                );

        // Set of packages to scan
        Set<String> resourcePackages = new HashSet<>();
        resourcePackages.add("fr.github.vera.resources");

        // Create Swagger configuration
        SwaggerConfiguration swaggerConfig = new SwaggerConfiguration()
                .openAPI(openAPI)
                .prettyPrint(true)
                .resourcePackages(resourcePackages)
                .readAllResources(true);

        try {
            // Initialize Swagger context
            new JaxrsOpenApiContextBuilder<>()
                    .application(this)
                    .openApiConfiguration(swaggerConfig)
                    .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException("Failed to initialize Swagger", e);
        }
    }

    public static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
        private final ObjectMapper mapper;

        public ObjectMapperContextResolver() {
            mapper = new ObjectMapper();
            // Important pour les dates
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            // Autres configurations si nécessaire
        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return mapper;
        }
    }
}