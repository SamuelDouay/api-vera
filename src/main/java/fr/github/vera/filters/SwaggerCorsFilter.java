package fr.github.vera.filters;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
public class SwaggerCorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Rien à faire dans la requête
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        String path = requestContext.getUriInfo().getPath();

        // Autoriser CORS pour tous les chemins Swagger/OpenAPI
        if (path.contains("openapi") || path.contains("swagger") || path.contains("/q/")) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
            responseContext.getHeaders().add("Access-Control-Allow-Headers",
                    "origin, content-type, accept, authorization");
            responseContext.getHeaders().add("Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        }
    }
}