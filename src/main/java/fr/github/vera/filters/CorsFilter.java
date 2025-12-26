package fr.github.vera.filters;

import fr.github.vera.config.ConfigProperties;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.lang.reflect.Method;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private boolean isPublicEndpoint(ContainerRequestContext requestContext) {
        try {
            // Récupérer la méthode de la ressource depuis le contexte
            Object resourceMethodInvoker = requestContext
                    .getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");

            if (resourceMethodInvoker != null) {
                // Utiliser la réflexion pour accéder à la méthode
                Method getMethod = resourceMethodInvoker.getClass().getMethod("getMethod");
                Method resourceMethod = (Method) getMethod.invoke(resourceMethodInvoker);

                // Vérifier si la méthode ou sa classe a l'annotation @Public
                return resourceMethod.isAnnotationPresent(Public.class) ||
                        resourceMethod.getDeclaringClass().isAnnotationPresent(Public.class);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        String origin = requestContext.getHeaderString("Origin");
        boolean isPublic = isPublicEndpoint(requestContext);

        setCorsHeaders(responseContext, origin, isPublic);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if ("OPTIONS".equals(requestContext.getMethod())) {
            // Pour les requêtes preflight, on doit déterminer si le chemin est public
            boolean isPublic = isPublicEndpoint(requestContext);
            String origin = requestContext.getHeaderString("Origin");

            Response.ResponseBuilder responseBuilder = Response.ok();
            setCorsHeaders(responseBuilder, origin, isPublic);
            requestContext.abortWith(responseBuilder.build());
        }
    }

    private void setCorsHeaders(ContainerResponseContext responseContext,
                                String origin, boolean isPublic) {

        if (isPublic) {
            // Endpoint public : toutes origines autorisées
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        } else {
            // Endpoint protégé : seulement les domaines autorisés
            if (origin != null && isOriginAllowed(origin)) {
                responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
                responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
            }
        }

        // Headers communs
        responseContext.getHeaders().add("Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization, x-requested-with, x-csrf-token");
        responseContext.getHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
        responseContext.getHeaders().add("Access-Control-Expose-Headers",
                "x-csrf-token, authorization");
    }

    private void setCorsHeaders(Response.ResponseBuilder responseBuilder,
                                String origin, boolean isPublic) {

        if (isPublic) {
            responseBuilder.header("Access-Control-Allow-Origin", "*");
        } else if (origin != null && isOriginAllowed(origin)) {
            responseBuilder.header("Access-Control-Allow-Origin", origin);
            responseBuilder.header("Access-Control-Allow-Credentials", "true");
        }

        // Headers communs
        responseBuilder.header("Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization, x-requested-with, x-csrf-token");
        responseBuilder.header("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        responseBuilder.header("Access-Control-Max-Age", "3600");
    }

    private boolean isOriginAllowed(String origin) {
        // Support multiple domains
        String allowedDomain = ConfigProperties.getInstance().getProperty("cors.allowed-domain");
        String[] domains = allowedDomain.split(",");
        for (String domain : domains) {
            String trimmed = domain.trim();
            if (origin.equals(trimmed)) {
                return true;
            }
            // Support pour localhost avec différents ports
            if (trimmed.startsWith("http://localhost") && origin.startsWith("http://localhost")) {
                return true;
            }
        }
        return false;
    }
}