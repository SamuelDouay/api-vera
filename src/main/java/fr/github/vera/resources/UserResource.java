package fr.github.vera.resources;

import fr.github.vera.filters.Secured;
import fr.github.vera.model.ResponseApi;
import fr.github.vera.model.User;
import fr.github.vera.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserResource {
    private final UserService userService = new UserService();

    @GET
    @Operation(
            summary = "Récupérer tous les utilisateurs",
            description = "Retourne la liste de tous les utilisateurs")
    @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = ResponseApi.class))
    )
    public Response getAllUsers(@QueryParam("limit") @DefaultValue("100") int limit,
                                @QueryParam("offset") @DefaultValue("0") int offset) {
        try {
            List<User> users = userService.getAllUsers(limit, offset);

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
    @Secured(roles = {"admin", "user"})
    @Operation(
            summary = "Récupérer un utilisateur par ID",
            description = "Retourne un utilisateur spécifique par son ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
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
            ResponseApi<String> errorResponse = new ResponseApi<>("Invalid user ID: must be a positive integer");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            ResponseApi<User> response = new ResponseApi<>(user.get());
            return Response.ok(response).build();
        } else {
            ResponseApi<String> errorResponse = new ResponseApi<>("User not found with ID: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }
    }

    @GET
    @Path("/email")
    @Secured(roles = {"admin", "user"})
    @Operation(
            summary = "Récupérer un utilisateur par email",
            description = "Retourne un utilisateur spécifique par son email"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user email",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            )
    })
    public Response getUserByEmail(@QueryParam("email") String email) {
        if (email == null || email.isBlank()) {
            ResponseApi<String> errorResponse = new ResponseApi<>("Email parameter is required and cannot be empty");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        if (!isValidEmailFormat(email)) {
            ResponseApi<String> errorResponse = new ResponseApi<>("Invalid email format");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }


        Optional<User> user = userService.getUserByEmail(email);
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

    private boolean isValidEmailFormat(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    @GET
    @Path("/count")
    @Secured(roles = {"admin"})
    @Operation(
            summary = "Récupérer le nombre total d'utilisateurs",
            description = "Retourne le nombre total d'utilisateur"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "return user size",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            )
    })
    public Response count() {
        Integer count = userService.count();
        ResponseApi<Integer> response = new ResponseApi<>(count);
        return Response.ok(response).build();
    }

    @POST
    @Secured(roles = {"admin", "user"})
    @Operation(
            summary = "Créer un nouvel utilisateur",
            description = "Crée un nouvel utilisateur avec les données fournies"
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
            if (!isValidEmailFormat(user.getEmail())) {
                ResponseApi<String> errorResponse = new ResponseApi<>("Invalid email format");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            User createdUser = userService.createUser(user);
            ResponseApi<User> response = new ResponseApi<>(createdUser);
            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .location(URI.create("/users/" + createdUser.getId()))
                    .build();
        } catch (ConstraintViolationException e) {
            ResponseApi<String> errorResponse = new ResponseApi<>(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
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
    @Secured(roles = {"admin", "user"})
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
        try {
            if (id == null || id <= 0) {
                ResponseApi<String> errorResponse = new ResponseApi<>("Invalid user ID");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            if (!isValidEmailFormat(user.getEmail())) {
                ResponseApi<String> errorResponse = new ResponseApi<>("Invalid email format");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            User updatedUser = userService.updateUser(id, user);
            if (updatedUser != null) {
                ResponseApi<User> response = new ResponseApi<>(updatedUser);
                return Response.ok(response).build();
            } else {
                ResponseApi<String> errorResponse = new ResponseApi<>("User not found");
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorResponse)
                        .build();
            }
        } catch (ConstraintViolationException e) {
            ResponseApi<String> errorResponse = new ResponseApi<>(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        } catch (Exception e) {
            ResponseApi<String> errorResponse = new ResponseApi<>("Failed to update user: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Secured(roles = {"admin"})
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