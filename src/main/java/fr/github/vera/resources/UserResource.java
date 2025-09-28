package fr.github.vera.resources;

import fr.github.vera.model.User;
import fr.github.vera.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.*;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserResource {
    private final UserService userService = new UserService();

    @GET
    @Operation(
            summary = "Récupérer tous les utilisateurs",
            description = "Retourne la liste de tous les utilisateurs",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès")
    public Response getAllUsers(@QueryParam("limit") @DefaultValue("100") int limit,
                                @QueryParam("offset") @DefaultValue("0") int offset) {
        try {
            Collection<User> allUsers = userService.getAllUsers();
            Map<String, Object> response = getStringObjectMap(limit, offset, allUsers);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to retrieve users", "message", e.getMessage()))
                    .build();
        }
    }

    private static Map<String, Object> getStringObjectMap(int limit, int offset, Collection<User> allUsers) {
        List<User> users = new ArrayList<>(allUsers);

        // Pagination
        int start = Math.min(offset, users.size());
        int end = Math.min(start + limit, users.size());
        List<User> paginatedUsers = users.subList(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("users", paginatedUsers);
        response.put("total", users.size());
        response.put("offset", offset);
        response.put("limit", limit);
        return response;
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Integer id) {
        if (id == null || id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid user ID"))
                    .build();
        }

        Optional<User> user = userService.getUserById(id);
        return user.map(u -> Response.ok(u).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build());
    }

    @POST
    @Operation(
            summary = "Créer un nouvel utilisateur",
            description = "Crée un nouvel utilisateur avec les données fournies",
            security = @SecurityRequirement(name = "BasicAuth")
    )
    @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    public Response createUser(@Valid User user) {
        try {
            User createdUser = userService.createUser(user);
            return Response.status(Response.Status.CREATED)
                    .entity(createdUser)
                    .location(URI.create("/users/" + createdUser.getId()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to create user", "message", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Integer id, @Valid User user) {
        if (id == null || id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid user ID"))
                    .build();
        }

        Optional<User> updatedUser = userService.updateUser(id, user);
        return updatedUser.map(u -> Response.ok(u).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build());
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Integer id) {
        if (id == null || id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid user ID"))
                    .build();
        }

        boolean deleted = userService.deleteUser(id);
        return deleted ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "User not found"))
                .build();
    }
}