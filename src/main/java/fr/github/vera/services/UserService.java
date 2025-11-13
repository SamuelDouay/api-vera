package fr.github.vera.services;

import fr.github.vera.model.User;
import fr.github.vera.repository.IUserRepository;
import fr.github.vera.repository.UserRepository;

import java.util.Optional;

public class UserService extends BaseService<User, Integer, IUserRepository> {

    public UserService() {
        super(new UserRepository());
    }

    public Optional<User> getUserByEmail(String email) {
        return repository.findByEmail(email);
    }
}
