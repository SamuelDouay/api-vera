package fr.github.vera.resources;

import fr.github.vera.exception.UserNotFoundException;
import fr.github.vera.filters.Secured;
import fr.github.vera.model.User;
import fr.github.vera.repository.IUserRepository;
import fr.github.vera.response.UserListResponse;
import fr.github.vera.response.UserResponse;
import fr.github.vera.services.BaseService;
import fr.github.vera.services.UserService;
import fr.github.vera.services.UserValidationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.function.Function;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserResource extends BaseResource<User, Integer, IUserRepository, UserResponse, UserListResponse> {
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
    protected Function<User, UserResponse> getResponseMapper() {
        return UserResponse::new;
    }

    @Override
    protected Function<List<User>, UserListResponse> getListResponseMapper() {
        return UserListResponse::new;
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
        validationService.validateEmail(entity.getEmail());
    }

    @Override
    protected void preUpdate(Integer id, User entity, SecurityContext securityContext) {
        validationService.validateUserAccess(id, null, securityContext, entity);
    }

    @Override
    protected void preDelete(Integer id, SecurityContext securityContext) {
        validationService.validateUserId(id);
    }

    @Override
    protected Integer getId(User entity) {
        return entity.getId();
    }

    // Méthodes spécifiques à User
    @GET
    @Path("/email")
    @Secured()
    public jakarta.ws.rs.core.Response getUserByEmail(
            @QueryParam("email") String email,
            @Context SecurityContext securityContext) {

        validationService.validateUserAccess(0, email, securityContext, null);

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserResponse response = new UserResponse(user);
        return jakarta.ws.rs.core.Response.ok(response).build();
    }
}