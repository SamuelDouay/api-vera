package fr.github.vera.repository;

import fr.github.vera.model.User;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends IRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    List<User> findUsersByName(String name, int limit, int offset);

    List<User> findAdmins(boolean isAdmin, int limit, int offset);

    List<User> findRecentUsers(int limit, int offset);

    boolean updatePassword(Integer userId, String hashedPassword);

    boolean updateProfile(Integer userId, String name, String surname, String email);

    boolean toggleAdminStatus(Integer userId);

    boolean deactivateUser(Integer userId);

    int countUsers();

    int countAdmins();

    boolean emailExists(String email);

    boolean emailExistsForOtherUser(String email, Integer userId);

    Optional<User> findByEmailAndPassword(String email, String hashedPassword);
}