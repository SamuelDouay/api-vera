package fr.github.vera.resources;

import fr.github.vera.exception.NotFoundException;
import fr.github.vera.filters.Secured;
import fr.github.vera.repository.IRepository;
import fr.github.vera.response.ListResponse;
import fr.github.vera.response.Response;
import fr.github.vera.services.BaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;

public abstract class BaseResource<T, I, R extends IRepository<T, I>> {

    protected abstract String getResourcePath();

    protected abstract BaseService<T, I, R> getService();

    protected abstract String getResourceName();

    // Méthodes optionnelles à override si nécessaire
    protected void preCreate(T entity, SecurityContext securityContext) {
    }

    protected void preUpdate(I id, T entity, SecurityContext securityContext) {
    }

    protected void preDelete(I id, SecurityContext securityContext) {
    }

    protected void validateAccess(I id, SecurityContext securityContext) {
    }

    protected String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header missing or invalid");
        }
        return authorizationHeader.substring(7); // Retire "Bearer "
    }

    @GET
    @Secured(adminOnly = true)
    @Operation(summary = "Récupérer toutes les ressources", description = "Retourne la liste de toutes les ressources")
    @ApiResponse(responseCode = "200", description = "Resources retrieved successfully")
    public jakarta.ws.rs.core.Response getAll(
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @Context SecurityContext securityContext) {

        List<T> items = getService().getAll(limit, offset);
        ListResponse<T> response = new ListResponse<>(items);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Secured()
    @Operation(summary = "Récupérer une ressource par ID", description = "Retourne une ressource spécifique par son ID")
    @ApiResponse(responseCode = "200", description = "Resource retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = Response.class)))
    public jakarta.ws.rs.core.Response getById(
            @PathParam("id") I id,
            @Context SecurityContext securityContext) {

        validateAccess(id, securityContext);

        T item = getService().getById(id)
                .orElseThrow(() -> new NotFoundException(getResourceName() + " not found with ID: " + id));
        Response<T> response = new Response<>(item);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PUT
    @Path("/{id}")
    @Secured()
    @Operation(summary = "Mettre à jour une ressource", description = "Met à jour une ressource existante")
    @ApiResponse(responseCode = "200", description = "Resource updated successfully")
    @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = Response.class)))
    public jakarta.ws.rs.core.Response update(
            @PathParam("id") I id,
            @Valid T entity,
            @Context SecurityContext securityContext) {

        validateAccess(id, securityContext);
        preUpdate(id, entity, securityContext);

        T updatedItem = getService().update(id, entity);
        if (updatedItem == null) {
            throw new NotFoundException(getResourceName() + " not found");
        }

        Response<T> response = new Response<>(updatedItem);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured(adminOnly = true)
    @Operation(summary = "Supprimer une ressource", description = "Supprime une ressource par son ID")
    @ApiResponse(responseCode = "204", description = "Resource deleted successfully")
    @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = Response.class)))
    public jakarta.ws.rs.core.Response delete(
            @PathParam("id") I id,
            @Context SecurityContext securityContext) {

        preDelete(id, securityContext);

        boolean deleted = getService().delete(id);
        if (!deleted) {
            throw new NotFoundException(getResourceName() + " not found");
        }

        return jakarta.ws.rs.core.Response.noContent().build();
    }

    @GET
    @Path("/count")
    @Secured(adminOnly = true)
    @Operation(summary = "Récupérer le nombre total de ressources", description = "Retourne le nombre total de ressources")
    @ApiResponse(responseCode = "200", description = "return resource count", content = @Content(schema = @Schema(implementation = Response.class)))
    public jakarta.ws.rs.core.Response count() {
        Response<Integer> response = new Response<>(getService().count());
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @POST
    @Secured(adminOnly = true)
    @Operation(summary = "Créer une nouvelle ressource", description = "Crée une nouvelle ressource avec les données fournies")
    @ApiResponse(responseCode = "201", description = "Resource created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid resource data", content = @Content(schema = @Schema(implementation = Response.class)))
    public jakarta.ws.rs.core.Response create(
            @Valid T entity,
            @Context SecurityContext securityContext,
            @Context UriInfo uriInfo) {

        preCreate(entity, securityContext);

        T createdItem = getService().create(entity);

        Response<T> response = new Response<>(createdItem);
        return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.CREATED)
                .entity(response)
                .location(URI.create(getResourcePath() + "/" + getId(createdItem)))
                .build();
    }

    protected I getId(T entity) {
        try {
            // Recherche le champ "id" dans la classe et ses superclasses
            Field idField = findIdField(entity.getClass());
            idField.setAccessible(true);

            @SuppressWarnings("unchecked")
            I id = (I) idField.get(entity);
            return id;

        } catch (Exception e) {
            throw new RuntimeException("Could not get ID from entity", e);
        }
    }

    // Méthode utilitaire pour trouver le champ id dans la hiérarchie de classes
    private Field findIdField(Class<?> clazz) {
        try {
            return clazz.getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            // Si pas trouvé dans la classe actuelle, cherche dans la superclasse
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return findIdField(superClass);
            }
            throw new RuntimeException("No 'id' field found in class hierarchy for " + clazz.getSimpleName(), e);
        }
    }
}