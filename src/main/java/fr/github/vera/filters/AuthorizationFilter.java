package fr.github.vera.filters;

import fr.github.vera.documention.ErrorResponseApi;
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

import java.lang.reflect.Method;

@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    private static final Logger log = LogManager.getLogger(AuthorizationFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        SecurityContext securityContext = requestContext.getSecurityContext();
        Secured secured = getSecuredAnnotation();

        if (secured != null) {
            boolean adminOnly = secured.adminOnly();
            String currentUser = securityContext.getUserPrincipal().getName();
            boolean isAdmin = securityContext.isUserInRole("admin");

            log.debug("Vérification accès - Utilisateur: {}, Admin: {}, adminOnly: {}",
                    currentUser, isAdmin, adminOnly);

            // Si la route est réservée aux admins ET que l'utilisateur n'est pas admin
            if (adminOnly && !isAdmin) {
                String errorMessage = "Accès refusé. Réservé aux administrateurs.";

                log.warn("Accès admin refusé pour l'utilisateur: {}", currentUser);

                ErrorResponseApi
                        errorResponse = new ErrorResponseApi(errorMessage);
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
            Secured classAnnotation = resourceClass.getAnnotation(Secured.class);
            if (classAnnotation != null) {
                return classAnnotation;
            }
        }
        return null;
    }
}