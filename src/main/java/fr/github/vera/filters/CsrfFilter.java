package fr.github.vera.filters;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class CsrfFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = LogManager.getLogger(CsrfFilter.class);
    private static final String CSRF_TOKEN_HEADER = "X-CSRF-Token";
    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";
    private static final String WEB_CLIENT = "web";
    private static final Map<String, String> tokenStore = new ConcurrentHashMap<>();


    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method) || isPublicEndpoint()) {
            return;
        }

        if (!isWebClient(requestContext)) {
            logger.debug("Client API - non web  - skip CSRF pour {} {}", method, path);
            return;
        }

        // Récupérer la session utilisateur
        String sessionId = getSessionId(requestContext);
        if (sessionId == null) {
            logger.warn("Session non trouvée pour la requête CSRF");
            return;
        }

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
        if ("GET".equals(requestContext.getMethod()) && isWebClient(requestContext)) {
            String sessionId = getSessionId(requestContext);
            if (sessionId != null) {
                String token = generateToken();
                tokenStore.put(sessionId, token);
                responseContext.getHeaders().add(CSRF_TOKEN_HEADER, token);
                responseContext.getHeaders().add("Access-Control-Expose-Headers", CSRF_TOKEN_HEADER);
                logger.debug("Token CSRF généré pour client web");
            }
        }
    }

    private boolean isWebClient(ContainerRequestContext requestContext) {
        String clientType = requestContext.getHeaderString(CLIENT_TYPE_HEADER);
        return WEB_CLIENT.equalsIgnoreCase(clientType);
    }

    private String getSessionId(ContainerRequestContext request) {
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

    private boolean isPublicEndpoint() {
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (method != null && method.isAnnotationPresent(Public.class)) {
            return true;
        }
        return resourceClass != null && resourceClass.isAnnotationPresent(Public.class);
    }
}