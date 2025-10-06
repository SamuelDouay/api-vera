package fr.github.vera.resources;

import fr.github.vera.exception.UserNotFoundException;
import fr.github.vera.filters.Secured;
import fr.github.vera.model.ResponseApi;
import fr.github.vera.model.User;
import fr.github.vera.services.UserService;
import fr.github.vera.services.UserValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserResource {
    private static final Logger log = LogManager.getLogger(UserResource.class);
    private final UserService userService = new UserService();
    private final UserValidationService validationService = new UserValidationService(userService);

    @GET
    @Secured(roles = {"admin"})
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
        List<User> users = userService.getAllUsers(limit, offset);
        List<User> paginatedUsers = applyPagination(users, limit, offset);
        Map<String, Object> meta = createPaginationMeta(users.size(), offset, limit, paginatedUsers.size());
        ResponseApi<List<User>> response = new ResponseApi<>(paginatedUsers, meta);
        return Response.ok(response).build();
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
    public Response getUserById(@PathParam("id") Integer id, @Context SecurityContext securityContext) {

        validationService.validateUserAccess(id, null, securityContext, null);

        User user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        ResponseApi<User> response = new ResponseApi<>(user);
        return Response.ok(response).build();
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
    public Response updateUser(@PathParam("id") Integer id, @Valid User user, @Context SecurityContext securityContext) {

        validationService.validateUserAccess(id, null, securityContext, user);

        User updatedUser = userService.updateUser(id, user);
        if (updatedUser == null) {
            throw new UserNotFoundException("User not found");
        }

        ResponseApi<User> response = new ResponseApi<>(updatedUser);
        return Response.ok(response).build();
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

        validationService.validateUserId(id);

        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            throw new UserNotFoundException("User not found");
        }

        return Response.noContent().build();
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
    public Response getUserByEmail(@QueryParam("email") String email, @Context SecurityContext securityContext) {

        validationService.validateUserAccess(0, email, securityContext, null);

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        ResponseApi<User> response = new ResponseApi<>(user);
        return Response.ok(response).build();
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
    @Secured(roles = {"admin"})
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

        validationService.validateEmail(user.getEmail());

        User createdUser = userService.createUser(user);

        ResponseApi<User> response = new ResponseApi<>(createdUser);
        return Response.status(Response.Status.CREATED)
                .entity(response)
                .location(URI.create("/users/" + createdUser.getId()))
                .build();
    }

    private List<User> applyPagination(List<User> users, int limit, int offset) {
        int start = Math.min(offset, users.size());
        int end = Math.min(start + limit, users.size());
        return users.subList(start, end);
    }

    private Map<String, Object> createPaginationMeta(int total, int offset, int limit, int returned) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("total", total);
        meta.put("offset", offset);
        meta.put("limit", limit);
        meta.put("returned", returned);
        return meta;
    }
}