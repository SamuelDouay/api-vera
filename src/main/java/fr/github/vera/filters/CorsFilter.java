package fr.github.vera.filters;

import fr.github.vera.config.ConfigProperties;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.lang.reflect.Method;

@Provider
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
            // Log l'erreur pour le debug
            System.err.println("Erreur lors de la détection de l'endpoint public: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Gérer les requêtes preflight OPTIONS
        if ("OPTIONS".equals(requestContext.getMethod())) {
            String origin = requestContext.getHeaderString("Origin");

            Response.ResponseBuilder responseBuilder = Response.ok();

            // Pour les OPTIONS, on autorise toujours (la sécurité sera gérée sur la vraie requête)
            if (origin != null && isOriginAllowed(origin)) {
                responseBuilder.header("Access-Control-Allow-Origin", origin);
                responseBuilder.header("Access-Control-Allow-Credentials", "true");
            } else {
                responseBuilder.header("Access-Control-Allow-Origin", "*");
            }

            responseBuilder.header("Access-Control-Allow-Headers",
                    "origin, content-type, accept, authorization, x-requested-with, x-csrf-token");
            responseBuilder.header("Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
            responseBuilder.header("Access-Control-Max-Age", "3600");
            responseBuilder.header("Access-Control-Expose-Headers",
                    "x-csrf-token, authorization");

            requestContext.abortWith(responseBuilder.build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        String origin = requestContext.getHeaderString("Origin");
        boolean isPublic = isPublicEndpoint(requestContext);

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

    private boolean isOriginAllowed(String origin) {
        try {
            String allowedDomain = ConfigProperties.getInstance().getProperty("cors.allowed-domain");

            if (allowedDomain == null || allowedDomain.isEmpty()) {
                System.err.println("ATTENTION: cors.allowed-domain n'est pas configuré!");
                return false;
            }

            // Log pour debug
            System.out.println("Origin reçue: " + origin);
            System.out.println("Domaines autorisés: " + allowedDomain);

            String[] domains = allowedDomain.split(",");
            for (String domain : domains) {
                String trimmed = domain.trim();

                // Comparaison exacte
                if (origin.equals(trimmed)) {
                    return true;
                }

                // Support pour localhost avec différents ports
                if (trimmed.startsWith("http://localhost") && origin.startsWith("http://localhost")) {
                    return true;
                }

                // Support pour localhost avec différents ports (https)
                if (trimmed.startsWith("https://localhost") && origin.startsWith("https://localhost")) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'origine: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}