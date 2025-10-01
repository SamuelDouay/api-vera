package fr.github.vera.database.dao;

import fr.github.vera.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao extends BaseDao{
    public UserDao() {
        super();
    }

    public List<User> getAllUser() {
        String sql = """
                SELECT * FROM users
                """;
        return executeQuery(sql, rs -> {
            List<User> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new User(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3)
                ));
            }
            return result;
        }, new ArrayList<>(), "GET ALL USERS");
    }

    public User getUserById(Integer id) {
        String sql = """
                SELECT * FROM users
                WHERE users.id = ?
                """;
        return executeQueryWithParams(sql, rs -> {
           User result = null;
            while (rs.next()) {
                result = new User(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3)
                );
            }
            return result;
        }, null, "GET USERS BY ID =" + id, id);
    }

    public void createUser(User user) {
        String sql = """
                INSERT INTO users (name, email) VALUES
                    (?, ?)
                """;
        executeUpdate(sql, "CREATE USER", user.getName(), user.getEmail());
    }

    public User getUserNameAndEmail(User user) {
        String sql = """
                SELECT * FROM users
                WHERE users.name = ? and users.email = ?
                """;
        return executeQueryWithParams(sql, rs -> {
            User result = null;
            while (rs.next()) {
                result = new User(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3)
                );
            }
            return result;
        }, null, "GET USERS BY user =" + user.getName(), user.getName(), user.getEmail());
    }

    public int deleteUserById(Integer id) {
        String sql = """
                DELETE FROM users
                WHERE users.id = ?
                """;
        return executeUpdate(sql, "DELETE USER ID :" + id, id);
    }
}
