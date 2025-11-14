package fr.github.vera.services;

import fr.github.vera.exception.AccessDeniedException;
import fr.github.vera.exception.InvalidDataException;
import fr.github.vera.exception.UserNotFoundException;
import fr.github.vera.model.User;
import jakarta.ws.rs.core.SecurityContext;

public record UserValidationService(UserService userService) {

    // Validation principale pour l'accès utilisateur
    public void validateUserAccess(Integer id, String email, SecurityContext securityContext, User userToUpdate) {
        validateUserIdIfPresent(id);
        validateEmailIfPresent(email);

        // Si l'utilisateur est admin (vérifié par le filtre @Secured), on skip les validations spécifiques
        if (!securityContext.isUserInRole("admin")) {
            User currentUser = getCurrentUser(securityContext);
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

    // Validation du format d'email
    public boolean isValidEmailFormat(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    // Validation pour la création d'utilisateur
    public void validateUserCreation(User user) {
        if (user == null) {
            throw new InvalidDataException("User data cannot be null");
        }
        validateEmail(user.getEmail());
        validateName(user.getName(), "Name");
        validateName(user.getSurname(), "Surname");
        validatePassword(user.getPassword());
    }

    // Validation pour la mise à jour d'utilisateur
    public void validateUserUpdate(Integer id, User user, SecurityContext securityContext) {
        validateUserId(id);
        validateUserAccess(id, null, securityContext, user);

        if (user.getEmail() != null) {
            validateEmail(user.getEmail());
            // Vérifier si l'email existe déjà pour un autre utilisateur
            if (userService.checkEmailExistsForOtherUser(user.getEmail(), id)) {
                throw new InvalidDataException("Email already exists for another user");
            }
        }

        if (user.getName() != null) {
            validateName(user.getName(), "Name");
        }

        if (user.getSurname() != null) {
            validateName(user.getSurname(), "Surname");
        }
    }

    // Validation du nom/prénom
    private void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException(fieldName + " cannot be empty");
        }
        if (name.length() > 255) {
            throw new InvalidDataException(fieldName + " cannot exceed 255 characters");
        }
    }

    // Validation du mot de passe
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidDataException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new InvalidDataException("Password must be at least 6 characters long");
        }
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
        // Un non-admin ne peut pas se donner les droits admin
        if (userToUpdate.isAdmin() && !currentUser.isAdmin()) {
            throw new AccessDeniedException("Access denied - you cannot change your role");
        }
        // Un non-admin ne peut pas modifier le rôle d'un autre utilisateur
        if (!currentUser.getId().equals(userToUpdate.getId()) && userToUpdate.isAdmin() != currentUser.isAdmin()) {
            throw new AccessDeniedException("Access denied - you cannot change other users' roles");
        }
    }

    private void validateEmailUpdate(User userToUpdate) {
        if (userToUpdate.getEmail() != null) {
            validateEmail(userToUpdate.getEmail());
        }
    }
}