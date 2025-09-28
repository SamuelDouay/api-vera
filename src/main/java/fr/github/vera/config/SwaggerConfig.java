package fr.github.vera.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "VERA API",
                version = "1.0.0",
                description = "API REST pour l'application VERA",
                contact = @Contact(
                        name = "Support VERA",
                        email = "support@vera.fr"
                )
        )
)
public class SwaggerConfig {
    // Cette classe sert uniquement pour les annotations Swagger
}
