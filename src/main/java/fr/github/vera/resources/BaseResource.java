package fr.github.vera.resources;

import fr.github.vera.exception.NotFoundException;
import fr.github.vera.filters.Secured;
import fr.github.vera.model.Count;
import fr.github.vera.repository.IRepository;
import fr.github.vera.response.CountResponse;
import fr.github.vera.response.Response;
import fr.github.vera.services.BaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BaseResource<T, ID, R extends IRepository<T, ID>, RESPONSE, LIST_RESPONSE> {

    protected abstract String getResourcePath();

    protected abstract BaseService<T, ID, R> getService();

    protected abstract Function<T, RESPONSE> getResponseMapper();

    protected abstract Function<List<T>, LIST_RESPONSE> getListResponseMapper();

    protected abstract String getResourceName();

    // Méthodes optionnelles à override si nécessaire
    protected void preCreate(T entity, SecurityContext securityContext) {
    }

    protected void preUpdate(ID id, T entity, SecurityContext securityContext) {
    }

    protected void preDelete(ID id, SecurityContext securityContext) {
    }

    protected void validateAccess(ID id, SecurityContext securityContext) {
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
        List<T> paginatedItems = applyPagination(items, limit, offset);
        Map<String, Object> meta = createPaginationMeta(items.size(), offset, limit, paginatedItems.size());

        LIST_RESPONSE response = getListResponseMapper().apply(paginatedItems);
        return addMetadataToResponse(response, meta);
    }

    @GET
    @Path("/{id}")
    @Secured()
    @Operation(summary = "Récupérer une ressource par ID", description = "Retourne une ressource spécifique par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = Response.class)))
    })
    public jakarta.ws.rs.core.Response getById(
            @PathParam("id") ID id,
            @Context SecurityContext securityContext) {

        validateAccess(id, securityContext);

        T item = getService().getById(id)
                .orElseThrow(() -> new NotFoundException(getResourceName() + " not found with ID: " + id));

        RESPONSE response = getResponseMapper().apply(item);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PUT
    @Path("/{id}")
    @Secured()
    @Operation(summary = "Mettre à jour une ressource", description = "Met à jour une ressource existante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource updated successfully"),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = Response.class)))
    })
    public jakarta.ws.rs.core.Response update(
            @PathParam("id") ID id,
            @Valid T entity,
            @Context SecurityContext securityContext) {

        validateAccess(id, securityContext);
        preUpdate(id, entity, securityContext);

        T updatedItem = getService().update(id, entity);
        if (updatedItem == null) {
            throw new NotFoundException(getResourceName() + " not found");
        }

        RESPONSE response = getResponseMapper().apply(updatedItem);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured(adminOnly = true)
    @Operation(summary = "Supprimer une ressource", description = "Supprime une ressource par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Resource deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = Response.class)))
    })
    public jakarta.ws.rs.core.Response delete(
            @PathParam("id") ID id,
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "return resource count", content = @Content(schema = @Schema(implementation = CountResponse.class)))
    })
    public jakarta.ws.rs.core.Response count() {
        CountResponse countResponse = new CountResponse(new Count(getService().count()));
        return jakarta.ws.rs.core.Response.ok(countResponse).build();
    }

    @POST
    @Secured(adminOnly = true)
    @Operation(summary = "Créer une nouvelle ressource", description = "Crée une nouvelle ressource avec les données fournies")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Resource created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid resource data", content = @Content(schema = @Schema(implementation = Response.class)))
    })
    public jakarta.ws.rs.core.Response create(
            @Valid T entity,
            @Context SecurityContext securityContext,
            @Context UriInfo uriInfo) {

        preCreate(entity, securityContext);

        T createdItem = getService().create(entity);

        RESPONSE response = getResponseMapper().apply(createdItem);
        return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.CREATED)
                .entity(response)
                .location(URI.create(getResourcePath() + "/" + getId(createdItem)))
                .build();
    }

    // Méthodes utilitaires
    protected List<T> applyPagination(List<T> items, int limit, int offset) {
        int start = Math.min(offset, items.size());
        int end = Math.min(start + limit, items.size());
        return items.subList(start, end);
    }

    protected Map<String, Object> createPaginationMeta(int total, int offset, int limit, int returned) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("total", total);
        meta.put("offset", offset);
        meta.put("limit", limit);
        meta.put("returned", returned);
        return meta;
    }

    protected jakarta.ws.rs.core.Response addMetadataToResponse(Object response, Map<String, Object> meta) {
        // Implémentation spécifique selon votre structure de réponse
        // Cette méthode peut être override dans les classes filles
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    protected abstract ID getId(T entity);
}