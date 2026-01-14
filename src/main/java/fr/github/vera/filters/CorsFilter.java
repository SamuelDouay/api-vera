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

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = LogManager.getLogger(CorsFilter.class);

    private static final String ALLOWED_HEADERS = "origin, content-type, accept, authorization, x-requested-with, x-csrf-token";
    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH";
    private static final String MAX_AGE = "3600";
    private static final String EXPOSED_HEADERS = "authorization";
    private static final String OPTIONS_METHOD = "OPTIONS";
    private static final String ORIGIN_HEADER = "Origin";
    private static final String REFERER_HEADER = "Referer";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private static final String CONFIG_KEY_ALLOWED_DOMAIN = "cors.allowed-domain";
    private static final String IS_PUBLIC_PROPERTY = "isPublic";

    public CorsFilter() {
        logInitialization();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String origin = requestContext.getHeaderString(ORIGIN_HEADER);
        String referer = requestContext.getHeaderString(REFERER_HEADER);

        logger.info("CORS REQUEST: {} {} | Origin: {} | Referer: {}", method, path, origin, referer);

        if (OPTIONS_METHOD.equals(method)) {
            handlePreflightRequest(requestContext, origin, referer, path);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        logger.debug("CORS RESPONSE: Status {}", responseContext.getStatus());

        if (responseContext.getHeaders().containsKey(ACCESS_CONTROL_ALLOW_ORIGIN)) {
            logger.debug("Headers CORS déjà présents, skip");
            return;
        }

        String origin = requestContext.getHeaderString(ORIGIN_HEADER);
        String referer = requestContext.getHeaderString(REFERER_HEADER);
        boolean isPublic = isPublic(requestContext);

        handleResponseCorsHeaders(requestContext, responseContext, origin, referer, isPublic);
    }

    public boolean isPublic(ContainerRequestContext requestContext) {
        Object property = requestContext.getProperty(IS_PUBLIC_PROPERTY);
        if (property == null) {
            return false;
        }
        return Boolean.TRUE.equals(property) || "true".equals(property.toString());
    }

    private void handlePreflightRequest(ContainerRequestContext requestContext,
                                        String origin, String referer, String path) {
        logger.info("PREFLIGHT détecté pour le path: {}", path);

        Response.ResponseBuilder responseBuilder = Response.ok();
        addCommonCorsHeaders(responseBuilder);

        // Vérifier Origin ou Referer
        if (isOriginAllowed(origin)) {
            responseBuilder.header(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            responseBuilder.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            logger.info("Origin autorisée: {}", origin);
        } else if (origin != null) {
            logger.warn("Origin REFUSÉE: {}", origin);
            responseBuilder.header(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        } else if (isRefererAllowed(referer)) {
            // Fallback sur Referer si Origin absent
            String allowedDomain = extractDomainFromReferer(referer);
            responseBuilder.header(ACCESS_CONTROL_ALLOW_ORIGIN, allowedDomain);
            responseBuilder.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            logger.info("Referer autorisé (Origin absent): {}", referer);
        }

        requestContext.abortWith(responseBuilder.build());
        logger.debug("Réponse PREFLIGHT envoyée");
    }

    private void handleResponseCorsHeaders(ContainerRequestContext requestContext,
                                           ContainerResponseContext responseContext,
                                           String origin, String referer, boolean isPublic) {
        logger.debug("Endpoint public: {} | Origin: {} | Referer: {}", isPublic, origin, referer);

        if (origin == null && referer == null) {
            handleMissingOriginAndReferer(responseContext, isPublic);
            return;
        }

        if (isPublic) {
            addPublicEndpointHeaders(responseContext);
        } else {
            handleProtectedEndpoint(responseContext, origin, referer);
        }

        if (responseContext.getStatus() != Response.Status.FORBIDDEN.getStatusCode()) {
            addCommonCorsHeaders(responseContext);
        }
    }

    private void handleMissingOriginAndReferer(ContainerResponseContext responseContext, boolean isPublic) {
        if (isPublic) {
            responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            logger.debug("Endpoint public sans Origin/Referer → Autorisé");
        } else {
            logger.warn("Endpoint protégé sans Origin ni Referer → BLOQUÉ");
            setForbiddenResponse(responseContext,
                    "{\"error\": \"CORS policy: Origin or Referer header is required for protected endpoints\"}");
        }
    }

    private void addPublicEndpointHeaders(ContainerResponseContext responseContext) {
        responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        logger.debug("CORS: * (endpoint public)");
    }

    private void handleProtectedEndpoint(ContainerResponseContext responseContext,
                                         String origin, String referer) {
        // Priorité 1 : Vérifier Origin
        if (isOriginAllowed(origin)) {
            responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            logger.debug("✓ CORS: {} (Origin autorisé)", origin);
            return;
        }

        // Priorité 2 : Fallback sur Referer
        if (isRefererAllowed(referer)) {
            String allowedDomain = extractDomainFromReferer(referer);
            responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN, allowedDomain);
            responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            logger.debug("✓ CORS: {} (Referer autorisé, Origin absent)", referer);
            return;
        }

        // Aucun header valide
        logger.warn("CORS: Origin/Referer NON autorisés - Origin: {} | Referer: {}", origin, referer);
        setForbiddenResponse(responseContext,
                String.format("{\"error\": \"CORS policy: Origin '%s' or Referer '%s' not allowed\"}",
                        origin, referer));
    }

    private boolean isRefererAllowed(String referer) {
        if (referer == null || referer.isEmpty()) {
            return false;
        }

        try {
            String allowedDomain = ConfigProperties.getInstance().getProperty(CONFIG_KEY_ALLOWED_DOMAIN);

            if (isDomainConfigurationInvalid(allowedDomain)) {
                logger.error("ATTENTION: {} n'est pas configuré!", CONFIG_KEY_ALLOWED_DOMAIN);
                return false;
            }

            String[] domains = allowedDomain.split(",");
            for (String domain : domains) {
                String trimmedDomain = domain.trim();

                // Vérifier si le Referer commence par le domaine autorisé
                if (referer.startsWith(trimmedDomain)) {
                    logger.debug("Referer match trouvé: {} commence par {}", referer, trimmedDomain);
                    return true;
                }

                // Support des localhost
                if (isLocalhostDomain(trimmedDomain) && referer.contains("localhost")) {
                    return true;
                }
            }

            logger.debug("Aucun match trouvé pour le Referer: {}", referer);
            return false;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du Referer: {}", e.getMessage(), e);
            return false;
        }
    }

    private String extractDomainFromReferer(String referer) {
        try {
            // Extraire le domaine depuis l'URL du Referer
            // Ex: https://vera.sadory.fr/dashboard → https://vera.sadory.fr
            if (referer.contains("://")) {
                int schemeEnd = referer.indexOf("://") + 3;
                int pathStart = referer.indexOf("/", schemeEnd);

                if (pathStart > 0) {
                    return referer.substring(0, pathStart);
                } else {
                    return referer;
                }
            }
            return referer;
        } catch (Exception e) {
            logger.warn("Impossible d'extraire le domaine du Referer: {}", referer);
            return referer;
        }
    }

    private void setForbiddenResponse(ContainerResponseContext responseContext, String errorMessage) {
        responseContext.setStatus(Response.Status.FORBIDDEN.getStatusCode());
        responseContext.setEntity(errorMessage);
        responseContext.getHeaders().putSingle(CONTENT_TYPE, APPLICATION_JSON);
    }

    private void addCommonCorsHeaders(Response.ResponseBuilder responseBuilder) {
        responseBuilder.header("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        responseBuilder.header("Access-Control-Allow-Methods", ALLOWED_METHODS);
        responseBuilder.header("Access-Control-Max-Age", MAX_AGE);
        responseBuilder.header("Access-Control-Expose-Headers", EXPOSED_HEADERS);
    }

    private void addCommonCorsHeaders(ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        responseContext.getHeaders().add("Access-Control-Allow-Methods", ALLOWED_METHODS);
        responseContext.getHeaders().add("Access-Control-Max-Age", MAX_AGE);
        responseContext.getHeaders().add("Access-Control-Expose-Headers", EXPOSED_HEADERS);
    }

    private boolean isOriginAllowed(String origin) {
        if (origin == null || origin.isEmpty()) {
            return false;
        }
        try {
            String allowedDomain = ConfigProperties.getInstance().getProperty(CONFIG_KEY_ALLOWED_DOMAIN);

            if (isDomainConfigurationInvalid(allowedDomain)) {
                logger.error("ATTENTION: {} n'est pas configuré!", CONFIG_KEY_ALLOWED_DOMAIN);
                return false;
            }

            return isOriginInAllowedDomains(origin, allowedDomain);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de l'origine: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean isDomainConfigurationInvalid(String allowedDomain) {
        return allowedDomain == null || allowedDomain.isEmpty();
    }

    private boolean isOriginInAllowedDomains(String origin, String allowedDomain) {
        String[] domains = allowedDomain.split(",");

        for (String domain : domains) {
            String trimmedDomain = domain.trim();
            logger.trace("Comparaison avec: '{}'", trimmedDomain);

            if (origin.equals(trimmedDomain) || isLocalhostDomain(trimmedDomain)) {
                logger.debug("Match trouvé pour l'origin: {} avec domaine: {}", origin, trimmedDomain);
                return true;
            }
        }

        logger.debug("Aucun match trouvé pour l'origin: {}", origin);
        return false;
    }

    private boolean isLocalhostDomain(String domain) {
        return domain.contains("localhost") ||
                domain.contains("127.0.0.1") ||
                domain.contains("0:0:0:");
    }

    private void logInitialization() {
        logger.info("================================================");
        logger.info("   CorsFilter INITIALISÉ avec support Referer !");
        logger.info("================================================");
    }
}