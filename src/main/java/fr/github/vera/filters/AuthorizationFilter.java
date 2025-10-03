package fr.github.vera.filters;

import fr.github.vera.model.ResponseApi;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        SecurityContext securityContext = requestContext.getSecurityContext();

        // Récupérer l'annotation @Secured de la méthode ou de la classe
        Secured secured = getSecuredAnnotation();

        if (secured != null) {
            String[] requiredRoles = secured.roles();

            // Vérifier si l'utilisateur a au moins un des rôles requis
            boolean hasRequiredRole = Arrays.stream(requiredRoles)
                    .anyMatch(securityContext::isUserInRole);

            if (!hasRequiredRole) {
                ResponseApi<String> errorResponse = new ResponseApi<>(
                        "Accès refusé. Rôle requis: " + String.join(", ", requiredRoles) +
                                ". Votre rôle: " + getCurrentUserRole(securityContext)
                );
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity(errorResponse)
                        .build());
            }
        }
    }

    private Secured getSecuredAnnotation() {
        // Vérifier d'abord sur la méthode
        Method method = resourceInfo.getResourceMethod();
        if (method != null) {
            Secured methodAnnotation = method.getAnnotation(Secured.class);
            if (methodAnnotation != null) {
                return methodAnnotation;
            }
        }

        // Vérifier sur la classe
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (resourceClass != null) {
            return resourceClass.getAnnotation(Secured.class);
        }

        return null;
    }

    private String getCurrentUserRole(SecurityContext securityContext) {
        // Méthode utilitaire pour debug
        if (securityContext.isUserInRole("admin")) return "admin";
        if (securityContext.isUserInRole("user")) return "user";
        return "AUCUN";
    }
}