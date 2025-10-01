package fr.github.vera.services;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import fr.github.vera.database.dao.UserDao;
import fr.github.vera.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserService {
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
        // Données de test
        User user1 = new User(idCounter.getAndIncrement(), "John Doe", "john@example.com");
        User user2 = new User(idCounter.getAndIncrement(), "Jane Smith", "jane@example.com");
        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);

    }

    public List<User> getAllUsers() {
        return userDao.getAllUser();
    }

    public User getUserById(Integer id) {
        return userDao.getUserById(id);
    }

    public User createUser(User user) {
        userDao.createUser(user);
        return userDao.getUserNameAndEmail(user);
    }

    public Optional<User> updateUser(Integer id, User updatedUser) {
        User existingUser = users.get(id);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            return Optional.of(existingUser);
        }
        return Optional.empty();
    }

    public boolean deleteUser(Integer id) {
        int remove = userDao.deleteUserById(id);
        return remove != 0;
    }
}
