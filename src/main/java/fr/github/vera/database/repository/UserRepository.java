package fr.github.vera.database.repository;

import fr.github.vera.database.DynamicUpdateBuilder;
import fr.github.vera.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository<User, Integer> implements IUserRepository {

    public UserRepository() {
        super("users", User.class);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return create(user);
        } else {
            return update(user);
        }
    }

    private User create(User user) {
        validateUserForCreation(user);
        String sql = "INSERT INTO users (name, surname, email, role, password) VALUES (?, ?, ?, ?, ?)";
        Integer generatedId = executeUpdateWithGeneratedKeys(sql,
                "CREATE USER", user.getName(), user.getSurname(), user.getEmail(), user.getRole(), user.getPassword());
        user.setId(generatedId);
        return user;
    }


    private User update(User user) {
        DynamicUpdateBuilder builder = new DynamicUpdateBuilder("users")
                .set("name", user.getName())
                .set("surname", user.getSurname())
                .set("email", user.getEmail())
                .set("password", user.getPassword())
                .set("role", user.getRole())
                .where("id = ?", user.getId());
        
        if (builder.hasUpdates()) {
            executeUpdate(builder.buildSql(), "UPDATE USER", builder.buildParams());
        }
        return user;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        return executeUpdate(sql, "DELETE USER", id) != 0;
    }

    @Override
    protected User mapResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("surname"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getString("password")
        );
    }

    @Override
    protected List<User> mapResultSetList(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(mapResultSet(rs));
        }
        return users;
    }

    // Méthodes spécifiques
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty(), Optional.empty(), "FIND USER BY EMAIL", email);
    }

    private void validateUserForCreation(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
    }
}