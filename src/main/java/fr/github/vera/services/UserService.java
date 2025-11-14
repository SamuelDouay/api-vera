package fr.github.vera.services;

import fr.github.vera.model.User;
import fr.github.vera.repository.IUserRepository;
import fr.github.vera.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService extends BaseService<User, Integer, IUserRepository> {

    public UserService() {
        super(new UserRepository());
    }

    // Méthode existante
    public Optional<User> getUserByEmail(String email) {
        return repository.findByEmail(email);
    }

    // Nouvelles méthodes utilitaires
    public List<User> getUsersByName(String name, int limit, int offset) {
        return repository.findUsersByName(name, limit, offset);
    }

    public List<User> getAdmins(boolean isAdmin, int limit, int offset) {
        return repository.findAdmins(isAdmin, limit, offset);
    }

    public List<User> getRecentUsers(int limit, int offset) {
        return repository.findRecentUsers(limit, offset);
    }

    public boolean updateUserPassword(Integer userId, String hashedPassword) {
        return repository.updatePassword(userId, hashedPassword);
    }

    public boolean updateUserProfile(Integer userId, String name, String surname, String email) {
        return repository.updateProfile(userId, name, surname, email);
    }

    public boolean toggleUserAdminStatus(Integer userId) {
        return repository.toggleAdminStatus(userId);
    }

    public boolean deactivateUser(Integer userId) {
        return repository.deactivateUser(userId);
    }

    public int getTotalUsers() {
        return repository.countUsers();
    }

    public int getTotalAdmins() {
        return repository.countAdmins();
    }

    public boolean checkEmailExists(String email) {
        return repository.emailExists(email);
    }

    public boolean checkEmailExistsForOtherUser(String email, Integer userId) {
        return repository.emailExistsForOtherUser(email, userId);
    }

    public Optional<User> authenticateUser(String email, String hashedPassword) {
        return repository.findByEmailAndPassword(email, hashedPassword);
    }
}