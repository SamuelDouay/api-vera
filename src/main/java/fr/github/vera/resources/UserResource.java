package fr.github.vera.resources;

import fr.github.vera.model.User;
import fr.github.vera.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = ResponseApi.class))
    )
    public Response getAllUsers(@QueryParam("limit") @DefaultValue("100") int limit,
                                @QueryParam("offset") @DefaultValue("0") int offset) {
        try {
            Collection<User> allUsers = userService.getAllUsers();
            List<User> users = new ArrayList<>(allUsers);

            // Pagination
            int start = Math.min(offset, users.size());
            int end = Math.min(start + limit, users.size());
            List<User> paginatedUsers = users.subList(start, end);

            // Metadata pour la pagination
            Map<String, Object> meta = new HashMap<>();
            meta.put("total", users.size());
            meta.put("offset", offset);
            meta.put("limit", limit);
            meta.put("returned", paginatedUsers.size());

            ResponseApi<List<User>> response = new ResponseApi<>(paginatedUsers, meta);
            return Response.ok(response).build();
        } catch (Exception e) {
            ResponseApi<String> errorResponse = new ResponseApi<>("Failed to retrieve users");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Récupérer un utilisateur par ID",
            description = "Retourne un utilisateur spécifique par son ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            )
    })
    public Response getUserById(@PathParam("id") Integer id) {
        if (id == null || id <= 0) {
            ResponseApi<String> errorResponse = new ResponseApi<>("Invalid user ID");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            ResponseApi<User> response = new ResponseApi<>(user.get());
            return Response.ok(response).build();
        } else {
            ResponseApi<String> errorResponse = new ResponseApi<>("User not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }
    }

    @POST
    @Operation(
            summary = "Créer un nouvel utilisateur",
            description = "Crée un nouvel utilisateur avec les données fournies",
            security = @SecurityRequirement(name = "BasicAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user data",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            )
    })
    public Response createUser(@Valid User user) {
        try {
            User createdUser = userService.createUser(user);
            ResponseApi<User> response = new ResponseApi<>(createdUser);
            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .location(URI.create("/users/" + createdUser.getId()))
                    .build();
        } catch (Exception e) {
            ResponseApi<String> errorResponse = new ResponseApi<>("Failed to create user: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Mettre à jour un utilisateur",
            description = "Met à jour un utilisateur existant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            )
    })
    public Response updateUser(@PathParam("id") Integer id, @Valid User user) {
        if (id == null || id <= 0) {
            ResponseApi<String> errorResponse = new ResponseApi<>("Invalid user ID");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        Optional<User> updatedUser = userService.updateUser(id, user);
        if (updatedUser.isPresent()) {
            ResponseApi<User> response = new ResponseApi<>(updatedUser.get());
            return Response.ok(response).build();
        } else {
            ResponseApi<String> errorResponse = new ResponseApi<>("User not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Supprimer un utilisateur",
            description = "Supprime un utilisateur par son ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            )
    })
    public Response deleteUser(@PathParam("id") Integer id) {
        if (id == null || id <= 0) {
            ResponseApi<String> errorResponse = new ResponseApi<>("Invalid user ID");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            ResponseApi<String> errorResponse = new ResponseApi<>("User not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }
    }
}