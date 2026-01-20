package fr.github.vera.filters;

import fr.github.vera.Main;
import fr.github.vera.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Method;
import java.security.Principal;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {
    private final JwtService jwtService = new JwtService();

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (isPublicEndpoint() || isPublicPath(requestContext)) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abortWithUnauthorized(requestContext, "Token JWT manquant");
            return;
        }

        String token = authHeader.substring(7);

        try {

            if (isTokenBlacklisted(token)) {
                abortWithUnauthorized(requestContext, "Token invalidé (déconnexion)");
                return;
            }

            // Valider le token
            Claims claims = jwtService.validateToken(token);
            String email = claims.getSubject();
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);

            if (isAdmin == null) {
                isAdmin = false;
            }

            requestContext.setSecurityContext(createSecurityContext(email, isAdmin));

        } catch (Exception e) {
            abortWithUnauthorized(requestContext, "Token JWT invalide: " + e.getMessage());
        }
    }

    private boolean isTokenBlacklisted(String token) {
        var blacklistService = Main.getTokenBlacklistService();
        return blacklistService != null && blacklistService.isTokenBlacklisted(token);

    }

    private boolean isPublicEndpoint() {
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (method != null && method.isAnnotationPresent(Public.class)) {
            return true;
        }
        return resourceClass != null && resourceClass.isAnnotationPresent(Public.class);
    }

    private boolean isPublicPath(ContainerRequestContext context) {
        String path = context.getUriInfo().getPath();
        return path.startsWith("swagger") || path.startsWith("openapi");
    }

    private SecurityContext createSecurityContext(String email, boolean isAdmin) {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> email;
            }

            @Override
            public boolean isUserInRole(String role) {
                if ("admin".equals(role)) {
                    return isAdmin;
                }
                return "user".equals(role);
            }

            @Override
            public boolean isSecure() {
                return false; // À adapter si HTTPS
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        };
    }

    private void abortWithUnauthorized(ContainerRequestContext context, String message) {
        fr.github.vera.response.Response<String> errorResponse = new fr.github.vera.response.Response<>(message);
        context.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(errorResponse)
                .build());
    }
}