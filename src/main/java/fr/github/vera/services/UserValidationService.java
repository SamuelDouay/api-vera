package fr.github.vera.services;

import fr.github.vera.exception.AccessDeniedException;
import fr.github.vera.exception.InvalidDataException;
import fr.github.vera.exception.UserNotFoundException;
import fr.github.vera.model.User;
import jakarta.ws.rs.core.SecurityContext;

public class UserValidationService {
    private final UserService userService;

    public UserValidationService(UserService userService) {
        this.userService = userService;
    }

    // Validation principale pour l'accès utilisateur
    public void validateUserAccess(Integer id, String email, SecurityContext securityContext, User userToUpdate) {
        validateUserIdIfPresent(id);
        validateEmailIfPresent(email);

        User currentUser = getCurrentUser(securityContext);

        if (!isAdminUser(securityContext)) {
            validateNonAdminAccess(id, email, currentUser, userToUpdate);
        }
    }

    // Validation de l'ID utilisateur
    public void validateUserId(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Invalid user ID: must be a positive integer");
        }
    }

    // Validation de l'email
    public void validateEmail(String email) {
        validateEmailRequired(email);
        validateEmailFormat(email);
    }

    // Validation de l'accès pour les non-administrateurs
    public void validateNonAdminAccess(Integer id, String email, User currentUser, User userToUpdate) {
        validateEmailAccess(email, currentUser);
        validateIdAccess(id, currentUser);
        validateUpdatePermissions(userToUpdate, currentUser);
    }

    // Récupération de l'utilisateur courant
    public User getCurrentUser(SecurityContext securityContext) {
        String currentUserEmail = securityContext.getUserPrincipal().getName();
        return userService.getUserByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
    }

    // Vérification des droits administrateur
    public boolean isAdminUser(SecurityContext securityContext) {
        return securityContext.isUserInRole("admin");
    }

    // Validation du format d'email
    public boolean isValidEmailFormat(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    // Méthodes privées pour une fonctionnalité spécifique chacune
    private void validateUserIdIfPresent(Integer id) {
        if (id != null && id < 0) {
            throw new InvalidDataException("Invalid user ID: must be a positive integer");
        }
    }

    private void validateEmailIfPresent(String email) {
        if (email != null) {
            validateEmail(email);
        }
    }

    private void validateEmailRequired(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidDataException("Email parameter is required and cannot be empty");
        }
    }

    private void validateEmailFormat(String email) {
        if (!isValidEmailFormat(email)) {
            throw new InvalidDataException("Invalid email format");
        }
    }

    private void validateEmailAccess(String email, User currentUser) {
        if (email != null && !currentUser.getEmail().equals(email)) {
            throw new AccessDeniedException("Access denied - you can only search your own email");
        }
    }

    private void validateIdAccess(Integer id, User currentUser) {
        if (id != null && id != 0 && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Access denied - you can only view your own profile");
        }
    }

    private void validateUpdatePermissions(User userToUpdate, User currentUser) {
        if (userToUpdate != null) {
            validateRoleChange(userToUpdate, currentUser);
            validateEmailUpdate(userToUpdate);
        }
    }

    private void validateRoleChange(User userToUpdate, User currentUser) {
        if (userToUpdate.isAdmin() && !currentUser.isAdmin())
            throw new AccessDeniedException("Access denied - you cannot change your role");
    }

    private void validateEmailUpdate(User userToUpdate) {
        if (userToUpdate.getEmail() != null) {
            validateEmail(userToUpdate.getEmail());
        }
    }
}