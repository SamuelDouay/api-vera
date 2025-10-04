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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    private static final Logger log = LogManager.getLogger(AuthorizationFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        SecurityContext securityContext = requestContext.getSecurityContext();

        // Récupérer l'annotation @Secured de la méthode ou de la classe
        Secured secured = getSecuredAnnotation();

        if (secured != null) {
            String[] requiredRoles = secured.roles();
            String currentUser = securityContext.getUserPrincipal().getName();

            // Vérifier si l'utilisateur a au moins un des rôles requis
            boolean hasRequiredRole = Arrays.stream(requiredRoles)
                    .anyMatch(securityContext::isUserInRole);

            if (!hasRequiredRole) {
                String currentRole = getCurrentUserRole(securityContext);
                String errorMessage = String.format(
                        "Accès refusé. Rôles requis: %s. Votre rôle: %s",
                        String.join(", ", requiredRoles), currentRole
                );

                log.warn("Accès refusé pour l'utilisateur: {} - {}", currentUser, errorMessage);

                ResponseApi<String> errorResponse = new ResponseApi<>(errorMessage);
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity(errorResponse)
                        .build());
            } else {
                log.info("Accès autorisé pour l'utilisateur: {}", currentUser);
            }
        } else {
            log.debug("Aucune annotation @Secured trouvée - pas de vérification de rôle nécessaire");
        }
    }

    private Secured getSecuredAnnotation() {
        // Vérifier d'abord sur la méthode
        Method method = resourceInfo.getResourceMethod();
        if (method != null) {
            Secured methodAnnotation = method.getAnnotation(Secured.class);
            if (methodAnnotation != null) {
                log.debug("Annotation @Secured trouvée sur la méthode: {}", method.getName());
                return methodAnnotation;
            }
        }

        // Vérifier sur la classe
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (resourceClass != null) {
            Secured classAnnotation = resourceClass.getAnnotation(Secured.class);
            if (classAnnotation != null) {
                log.debug("Annotation @Secured trouvée sur la classe: {}", resourceClass.getSimpleName());
                return classAnnotation;
            }
        }
        return null;
    }

    private String getCurrentUserRole(SecurityContext securityContext) {
        if (securityContext.isUserInRole("admin")) return "admin";
        if (securityContext.isUserInRole("user")) return "user";
        return "AUCUN_ROLE_TROUVE";
    }
}