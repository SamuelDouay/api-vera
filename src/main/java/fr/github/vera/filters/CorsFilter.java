package fr.github.vera.filters;

import fr.github.vera.config.ConfigProperties;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = LogManager.getLogger(CorsFilter.class);

    public CorsFilter() {
        logger.info("================================================");
        logger.info("   CorsFilter INITIALISÉ !");
        logger.info("================================================");
    }

    private boolean isPublicEndpoint(ContainerRequestContext requestContext) {
        try {
            // Pour Jersey, utiliser ResourceInfo
            Object resourceInfo = requestContext.getProperty("org.glassfish.jersey.server.model.ResourceMethod");

            if (resourceInfo == null) {
                // Fallback pour RESTEasy
                resourceInfo = requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
            }

            if (resourceInfo != null) {
                Method getMethod = resourceInfo.getClass().getMethod("getInvocable");
                Object invocable = getMethod.invoke(resourceInfo);

                Method getHandlingMethod = invocable.getClass().getMethod("getHandlingMethod");
                Method resourceMethod = (Method) getHandlingMethod.invoke(invocable);

                boolean isPublic = resourceMethod.isAnnotationPresent(Public.class) ||
                        resourceMethod.getDeclaringClass().isAnnotationPresent(Public.class);

                logger.debug("Méthode {} - Public: {}", resourceMethod.getName(), isPublic);
                return isPublic;
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la détection de l'endpoint public: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String origin = requestContext.getHeaderString("Origin");

        logger.info("CORS REQUEST: {} {} | Origin: {}", method, path, origin);

        // Gérer les requêtes preflight OPTIONS
        if ("OPTIONS".equals(method)) {
            logger.info("PREFLIGHT détecté pour le path: {}", path);

            Response.ResponseBuilder responseBuilder = Response.ok();

            // Pour les OPTIONS, autoriser si l'origine est valide
            if (origin != null && isOriginAllowed(origin)) {
                responseBuilder.header("Access-Control-Allow-Origin", origin);
                responseBuilder.header("Access-Control-Allow-Credentials", "true");
                logger.info("Origin autorisée: {}", origin);
            } else if (origin != null) {
                logger.warn("Origin REFUSÉE: {}", origin);
                // On autorise quand même pour le preflight, la vraie requête sera bloquée
                responseBuilder.header("Access-Control-Allow-Origin", origin);
            }

            responseBuilder.header("Access-Control-Allow-Headers",
                    "origin, content-type, accept, authorization, x-requested-with, x-csrf-token");
            responseBuilder.header("Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
            responseBuilder.header("Access-Control-Max-Age", "3600");
            responseBuilder.header("Access-Control-Expose-Headers",
                    "x-csrf-token, authorization");

            requestContext.abortWith(responseBuilder.build());
            logger.debug("Réponse PREFLIGHT envoyée");
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        logger.debug("CORS RESPONSE: Status {}", responseContext.getStatus());

        String origin = requestContext.getHeaderString("Origin");

        // Éviter de dupliquer les headers si déjà ajoutés par le preflight
        if (responseContext.getHeaders().containsKey("Access-Control-Allow-Origin")) {
            logger.debug("Headers CORS déjà présents, skip");
            return;
        }

        boolean isPublic = isPublicEndpoint(requestContext);
        logger.debug("Endpoint public: {} | Origin: {}", isPublic, origin);

        if (isPublic) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
            logger.debug("CORS: * (endpoint public)");
        } else {
            if (origin != null && isOriginAllowed(origin)) {
                responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
                responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
                logger.debug("CORS: {} (autorisé)", origin);
            } else {
                logger.warn("CORS: Origin non autorisée ou absente: {}", origin);
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

            logger.debug("Vérification origin: '{}' contre domaines autorisés: '{}'", origin, allowedDomain);

            if (allowedDomain == null || allowedDomain.isEmpty()) {
                logger.error("ATTENTION: cors.allowed-domain n'est pas configuré!");
                return false;
            }

            String[] domains = allowedDomain.split(",");
            for (String domain : domains) {
                String trimmed = domain.trim();

                logger.trace("Comparaison avec: '{}'", trimmed);

                // Comparaison exacte
                if (origin.equals(trimmed)) {
                    logger.debug("Match exact trouvé: {}", trimmed);
                    return true;
                }

                // Support pour localhost avec différents ports
                if (trimmed.startsWith("http://localhost") && origin.startsWith("http://localhost")) {
                    logger.debug("Match localhost HTTP trouvé");
                    return true;
                }

                if (trimmed.startsWith("https://localhost") && origin.startsWith("https://localhost")) {
                    logger.debug("Match localhost HTTPS trouvé");
                    return true;
                }
            }

            logger.debug("Aucun match trouvé pour l'origin: {}", origin);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de l'origine: {}", e.getMessage(), e);
        }

        return false;
    }
}