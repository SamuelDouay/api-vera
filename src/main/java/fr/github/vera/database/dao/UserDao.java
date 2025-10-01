package fr.github.vera.database.dao;

import fr.github.vera.database.DatabaseManager;
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
}
