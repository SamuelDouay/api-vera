package fr.github.vera.config;

import fr.github.vera.exception.GlobalExceptionMapper;
import fr.github.vera.filters.CorsFilter;
import fr.github.vera.resources.HealthResource;
import fr.github.vera.resources.MetricsResource;
import fr.github.vera.resources.SwaggerUIResource;
import fr.github.vera.resources.UserResource;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        // Register resources
        register(UserResource.class);
        register(HealthResource.class);
        register(MetricsResource.class);

        // Register filters and exception mappers
        register(CorsFilter.class);
        register(GlobalExceptionMapper.class);

        // Configure Swagger
        configureSwagger();

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
}