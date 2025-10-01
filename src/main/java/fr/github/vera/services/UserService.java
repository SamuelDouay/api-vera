package fr.github.vera.services;

import fr.github.vera.database.repository.IUserRepository;
import fr.github.vera.database.repository.UserRepository;
import fr.github.vera.model.User;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final IUserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public List<User> getAllUsers(int limit, int offset) {
        return userRepository.findAll(limit, offset);
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Integer id, User user) {
        user.setId(id);
        return userRepository.save(user);
    }

    public boolean deleteUser(Integer id) {
        return userRepository.delete(id);
    }

    public int count() {
        return userRepository.count();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
