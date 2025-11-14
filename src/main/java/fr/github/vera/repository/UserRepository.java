package fr.github.vera.repository;

import fr.github.vera.database.BaseRepository;
import fr.github.vera.model.User;

import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository<User, Integer> implements IUserRepository {

    public UserRepository() {
        super("users", User.class);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty(),
                Optional.empty(), "FIND USER BY EMAIL", email);
    }

    // Méthodes supplémentaires pour la gestion des utilisateurs

    @Override
    public List<User> findUsersByName(String name, int limit, int offset) {
        String sql = "SELECT * FROM users WHERE name ILIKE ? ORDER BY name, surname LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND USERS BY NAME", "%" + name + "%", limit, offset);
    }

    @Override
    public List<User> findAdmins(boolean isAdmin, int limit, int offset) {
        String sql = "SELECT * FROM users WHERE is_admin = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND ADMINS", isAdmin, limit, offset);
    }

    @Override
    public List<User> findRecentUsers(int limit, int offset) {
        String sql = "SELECT * FROM users ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, this::mapResultSetList, List.of(),
                "FIND RECENT USERS", limit, offset);
    }

    @Override
    public boolean updatePassword(Integer userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        return executeUpdate(sql, "UPDATE USER PASSWORD", hashedPassword, userId) != 0;
    }

    @Override
    public boolean updateProfile(Integer userId, String name, String surname, String email) {
        String sql = "UPDATE users SET name = ?, surname = ?, email = ? WHERE id = ?";
        return executeUpdate(sql, "UPDATE USER PROFILE", name, surname, email, userId) != 0;
    }

    @Override
    public boolean toggleAdminStatus(Integer userId) {
        String sql = "UPDATE users SET is_admin = NOT is_admin WHERE id = ?";
        return executeUpdate(sql, "TOGGLE ADMIN STATUS", userId) != 0;
    }

    @Override
    public boolean deactivateUser(Integer userId) {
        // Si vous avez un champ is_active, sinon cette méthode peut être supprimée
        String sql = "UPDATE users SET is_active = false WHERE id = ?";
        return executeUpdate(sql, "DEACTIVATE USER", userId) != 0;
    }

    @Override
    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        return executeQuery(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0, "COUNT USERS");
    }

    @Override
    public int countAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_admin = true";
        return executeQuery(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0, "COUNT ADMINS");
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        int count = executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "CHECK EMAIL EXISTS", email);
        return count > 0;
    }

    @Override
    public boolean emailExistsForOtherUser(String email, Integer userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
        int count = executeQueryWithParams(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0,
                "CHECK EMAIL EXISTS FOR OTHER USER", email, userId);
        return count > 0;
    }

    @Override
    public Optional<User> findByEmailAndPassword(String email, String hashedPassword) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty(),
                Optional.empty(), "FIND USER BY EMAIL AND PASSWORD", email, hashedPassword);
    }
}