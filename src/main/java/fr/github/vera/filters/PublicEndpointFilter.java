package fr.github.vera.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Public  // ← IMPORTANT : Lier ce filtre à l'annotation @Public
public class PublicEndpointFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Pré-traitement pour les endpoints publics
        requestContext.setProperty("isPublic", true);
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        // Post-traitement
        if (Boolean.TRUE.equals(requestContext.getProperty("isPublic"))) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        }
    }
}