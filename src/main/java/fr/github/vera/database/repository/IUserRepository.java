package fr.github.vera.database.repository;

import fr.github.vera.model.User;

import java.util.Optional;

public interface IUserRepository extends IRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}