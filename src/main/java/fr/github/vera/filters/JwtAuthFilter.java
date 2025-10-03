package fr.github.vera.filters;

import fr.github.vera.model.ResponseApi;
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

        // Extraire le token JWT
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abortWithUnauthorized(requestContext, "Token JWT manquant");
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Valider le token
            Claims claims = jwtService.validateToken(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            Integer userId = claims.get("userId", Integer.class);

            // Définir le contexte de sécurité
            requestContext.setSecurityContext(createSecurityContext(email, role, userId));

        } catch (Exception e) {
            abortWithUnauthorized(requestContext, "Token JWT invalide: " + e.getMessage());
        }
    }

    private boolean isPublicEndpoint(String path, String method) {
        // Endpoints publics
        return (path.equals("auth/login") && method.equals("POST")) ||
                (path.equals("auth/forgot") && method.equals("POST")) ||
                (path.equals("auth/register") && method.equals("POST")) ||
                (path.equals("auth/reset") && method.equals("POST")) ||
                (path.startsWith("swagger") || path.startsWith("openapi")); // Documentation
    }

    private boolean isPublicEndpoint() {
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();

        // Vérifier l'annotation sur la méthode
        if (method != null && method.isAnnotationPresent(Public.class)) {
            return true;
        }

        // Vérifier l'annotation sur la classe
        return resourceClass != null && resourceClass.isAnnotationPresent(Public.class);
    }

    private boolean isPublicPath(ContainerRequestContext context) {
        String path = context.getUriInfo().getPath();
        return path.startsWith("swagger") || path.startsWith("openapi");
    }

    private SecurityContext createSecurityContext(String email, String role, Integer userId) {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> email;
            }

            @Override
            public boolean isUserInRole(String requiredRole) {
                return role != null && role.equals(requiredRole);
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
        ResponseApi<String> errorResponse = new ResponseApi<>(message);
        context.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(errorResponse)
                .build());
    }
}