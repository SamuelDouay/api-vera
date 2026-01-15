package fr.github.vera.filters;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class CsrfFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = LogManager.getLogger(CsrfFilter.class);
    private static final String CSRF_TOKEN_HEADER = "x-csrf-token";
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";
    private static final String WEB_CLIENT = "web";
    private static final String IS_PUBLIC_PROPERTY = "isPublic";

    // Stockage en mémoire (session utilisateur -> token)
    private static final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        // Méthodes sûres : pas besoin de vérifier CSRF
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return;
        }

        if (isPublic(requestContext)) {
            return;
        }

        if (!isWebClient(requestContext)) {
            logger.debug("Client non-web détecté - skip CSRF pour: {} {}", method, path);
            return;
        }

        // Récupérer la session utilisateur
        String sessionId = getSessionId(requestContext);
        if (sessionId == null) {
            logger.warn("Session non trouvée pour la requête CSRF");
            return; // Laisser le filtre d'auth gérer
        }

        // Vérifier le token CSRF
        String csrfToken = requestContext.getHeaderString(CSRF_TOKEN_HEADER);
        String storedToken = tokenStore.get(sessionId);

        if (csrfToken == null || !csrfToken.equals(storedToken)) {
            logger.warn("CSRF token invalide pour la session: {}", sessionId);
            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"error\": \"CSRF token invalid or missing\"}")
                            .build()
            );
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String sessionId = getSessionId(requestContext);

        // Pour les requêtes GET, générer un nouveau token si besoin
        if ("GET".equals(requestContext.getMethod()) && sessionId != null && isWebClient(requestContext)) {
            String token = generateToken();
            tokenStore.put(sessionId, token);
            responseContext.getHeaders().add(CSRF_TOKEN_HEADER, token);
            responseContext.getHeaders().add("Access-Control-Expose-Headers", CSRF_TOKEN_HEADER);
            clearInsecureCookie(responseContext);
        }
    }

    public boolean isPublic(ContainerRequestContext requestContext) {
        Object property = requestContext.getProperty(IS_PUBLIC_PROPERTY);
        if (property == null) {
            return false;
        }
        return Boolean.TRUE.equals(property) || "true".equals(property.toString());
    }

    private boolean isWebClient(ContainerRequestContext requestContext) {
        String clientType = requestContext.getHeaderString(CLIENT_TYPE_HEADER);
        return WEB_CLIENT.equalsIgnoreCase(clientType);
    }

    /**
     * Supprime l'ancien cookie XSRF-TOKEN non sécurisé
     */
    private void clearInsecureCookie(ContainerResponseContext responseContext) {
        NewCookie deleteCookie = new NewCookie.Builder(CSRF_COOKIE_NAME)
                .value("")
                .path("/")
                .maxAge(0)  // Expire immédiatement
                .build();
        responseContext.getHeaders().add("Set-Cookie", deleteCookie);
    }

    private String getSessionId(ContainerRequestContext request) {
        // Récupérer l'ID de session depuis le cookie ou header
        Cookie sessionCookie = request.getCookies().get("JSESSIONID");
        if (sessionCookie != null) {
            return sessionCookie.getValue();
        }

        String authHeader = request.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            return Integer.toHexString(jwtToken.hashCode());
        }

        return null;
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}