package fr.github.vera.database.repository;

import fr.github.vera.model.User;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends IRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Pour la gestion des rôles plus tard
    List<User> findByRole(String role);
}