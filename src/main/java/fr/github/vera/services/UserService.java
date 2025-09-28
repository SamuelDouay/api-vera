package fr.github.vera.services;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import fr.github.vera.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserService {
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final MetricRegistry metrics = new MetricRegistry();

    // Métriques
    private final Timer getUserTimer = metrics.timer("user.get");
    private final Timer createUserTimer = metrics.timer("user.create");
    private final Counter userCount = metrics.counter("user.count");

    public UserService() {
        // Données de test
        User user1 = new User(idCounter.getAndIncrement(), "John Doe", "john@example.com", 30);
        User user2 = new User(idCounter.getAndIncrement(), "Jane Smith", "jane@example.com", 25);
        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
        userCount.inc(2);
    }

    public Collection<User> getAllUsers() {
        try (Timer.Context context = getUserTimer.time()) {
            return new ArrayList<>(users.values());
        }
    }

    public Optional<User> getUserById(Integer id) {
        try (Timer.Context context = getUserTimer.time()) {
            return Optional.ofNullable(users.get(id));
        }
    }

    public User createUser(User user) {
        try (Timer.Context context = createUserTimer.time()) {
            user.setId(idCounter.getAndIncrement());
            users.put(user.getId(), user);
            userCount.inc();
            return user;
        }
    }

    public Optional<User> updateUser(Integer id, User updatedUser) {
        User existingUser = users.get(id);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setAge(updatedUser.getAge());
            return Optional.of(existingUser);
        }
        return Optional.empty();
    }

    public boolean deleteUser(Integer id) {
        User removed = users.remove(id);
        if (removed != null) {
            userCount.dec();
            return true;
        }
        return false;
    }

    public MetricRegistry getMetrics() {
        return metrics;
    }

    public int getUserCount() {
        return users.size();
    }
}
