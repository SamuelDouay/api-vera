package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.User;

import java.util.Optional;

public class UserRepository extends BaseRepository<User, Integer> implements IUserRepository {

    public UserRepository() {
        super("users", User.class);
    }

    // Méthodes spécifiques
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty(), Optional.empty(), "FIND USER BY EMAIL", email);
    }
}