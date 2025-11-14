package fr.github.vera.resources;

import fr.github.vera.exception.InvalidDataException;
import fr.github.vera.exception.UserNotFoundException;
import fr.github.vera.filters.Secured;
import fr.github.vera.model.User;
import fr.github.vera.repository.IUserRepository;
import fr.github.vera.response.ListResponse;
import fr.github.vera.response.Response;
import fr.github.vera.services.BaseService;
import fr.github.vera.services.UserService;
import fr.github.vera.services.UserValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserResource extends BaseResource<User, Integer, IUserRepository> {
    private final UserService userService = new UserService();
    private final UserValidationService validationService = new UserValidationService(userService);

    @Override
    protected String getResourcePath() {
        return "/users";
    }

    @Override
    protected BaseService<User, Integer, IUserRepository> getService() {
        return userService;
    }

    @Override
    protected String getResourceName() {
        return "User";
    }

    @Override
    protected void validateAccess(Integer id, SecurityContext securityContext) {
        validationService.validateUserAccess(id, null, securityContext, null);
    }

    @Override
    protected void preCreate(User entity, SecurityContext securityContext) {
        validationService.validateUserCreation(entity);
        // Vérifier si l'email existe déjà
        if (userService.checkEmailExists(entity.getEmail())) {
            throw new InvalidDataException("Email already exists");
        }
    }

    @Override
    protected void preUpdate(Integer id, User entity, SecurityContext securityContext) {
        validationService.validateUserUpdate(id, entity, securityContext);
    }

    @Override
    protected void preDelete(Integer id, SecurityContext securityContext) {
        validationService.validateUserId(id);
    }

    // === ENDPOINTS EXISTANTS ===

    @GET
    @Path("/email")
    @Secured()
    @Operation(summary = "Récupérer un utilisateur par email")
    public jakarta.ws.rs.core.Response getUserByEmail(
            @QueryParam("email") String email,
            @Context SecurityContext securityContext) {

        validationService.validateUserAccess(0, email, securityContext, null);

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Response<User> response = new Response<>(user);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    // === NOUVEAUX ENDPOINTS UTILITAIRES ===

    @GET
    @Path("/search")
    @Secured(adminOnly = true)
    @Operation(summary = "Rechercher des utilisateurs par nom")
    public jakarta.ws.rs.core.Response searchUsersByName(
            @QueryParam("name") String name,
            @QueryParam("limit") @DefaultValue("50") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @Context SecurityContext securityContext) {

        List<User> users = userService.getUsersByName(name, limit, offset);
        ListResponse<User> response = new ListResponse<>(users);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/admins")
    @Secured(adminOnly = true)
    @Operation(summary = "Récupérer les administrateurs")
    public jakarta.ws.rs.core.Response getAdmins(
            @QueryParam("isAdmin") @DefaultValue("true") boolean isAdmin,
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @Context SecurityContext securityContext) {

        List<User> admins = userService.getAdmins(isAdmin, limit, offset);
        ListResponse<User> response = new ListResponse<>(admins);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/recent")
    @Secured(adminOnly = true)
    @Operation(summary = "Récupérer les utilisateurs récents")
    public jakarta.ws.rs.core.Response getRecentUsers(
            @QueryParam("limit") @DefaultValue("50") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @Context SecurityContext securityContext) {

        List<User> users = userService.getRecentUsers(limit, offset);
        ListResponse<User> response = new ListResponse<>(users);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/password")
    @Secured()
    @Operation(summary = "Mettre à jour le mot de passe")
    public jakarta.ws.rs.core.Response updatePassword(
            @PathParam("id") Integer id,
            @FormParam("password") String hashedPassword,
            @Context SecurityContext securityContext) {

        validationService.validateUserAccess(id, null, securityContext, null);

        boolean success = userService.updateUserPassword(id, hashedPassword);
        Response<Boolean> response = new Response<>(success);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/profile")
    @Secured()
    @Operation(summary = "Mettre à jour le profil utilisateur")
    public jakarta.ws.rs.core.Response updateProfile(
            @PathParam("id") Integer id,
            @FormParam("name") String name,
            @FormParam("surname") String surname,
            @FormParam("email") String email,
            @Context SecurityContext securityContext) {

        validationService.validateUserAccess(id, null, securityContext, null);
        validationService.validateEmail(email);

        boolean success = userService.updateUserProfile(id, name, surname, email);
        Response<Boolean> response = new Response<>(success);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/admin")
    @Secured(adminOnly = true)
    @Operation(summary = "Modifier le statut administrateur")
    public jakarta.ws.rs.core.Response toggleAdminStatus(
            @PathParam("id") Integer id,
            @Context SecurityContext securityContext) {

        boolean success = userService.toggleUserAdminStatus(id);
        Response<Boolean> response = new Response<>(success);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/stats/count")
    @Secured(adminOnly = true)
    @Operation(summary = "Obtenir les statistiques des utilisateurs")
    public jakarta.ws.rs.core.Response getUserStats(@Context SecurityContext securityContext) {

        int totalUsers = userService.getTotalUsers();
        int totalAdmins = userService.getTotalAdmins();

        UserStats stats = new UserStats(totalUsers, totalAdmins);
        Response<UserStats> response = new Response<>(stats);

        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @GET
    @Path("/email/check")
    @Operation(summary = "Vérifier si un email existe")
    public jakarta.ws.rs.core.Response checkEmailExists(@QueryParam("email") String email) {
        boolean exists = userService.checkEmailExists(email);
        Response<Boolean> response = new Response<>(exists);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    // Classe interne pour les statistiques
    public record UserStats(int totalUsers, int totalAdmins) {
    }
}