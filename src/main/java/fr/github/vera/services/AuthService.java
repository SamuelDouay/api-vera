package fr.github.vera.services;

import fr.github.vera.model.User;
import fr.github.vera.model.authentification.*;
import fr.github.vera.repository.IUserRepository;
import fr.github.vera.repository.UserRepository;
import fr.github.vera.security.JwtService;
import fr.github.vera.security.PasswordEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class AuthService {
    private static final Logger log = LogManager.getLogger(AuthService.class);
    private final IUserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.jwtService = new JwtService();
        this.passwordEncoder = new PasswordEncoder();
    }

    public AuthResponse authenticate(LoginRequest request) {
        // 1. Trouver l'utilisateur par email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email incorrect");
        }

        User user = userOpt.get();

        // 2. Vérifier le mot de passe
        if (!passwordEncoder.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        // 3. Générer les tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.isAdmin());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        // 4. Retourner la réponse
        return new AuthResponse(accessToken, refreshToken, user);
    }

    public AuthResponse register(RegisterRequest request) {
        // 1. Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // 2. Hasher le mot de passe
        String hashedPassword = passwordEncoder.hashPassword(request.getPassword());

        // 3. Créer le nouvel utilisateur
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setSurname(request.getSurname());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(hashedPassword);
        newUser.setAdmin(false); // Rôle par défaut

        // 4. Sauvegarder l'utilisateur
        User savedUser = userRepository.save(newUser);

        // 5. Générer les tokens
        String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.isAdmin());
        String refreshToken = jwtService.generateRefreshToken(savedUser.getEmail());

        return new AuthResponse(accessToken, refreshToken, savedUser);
    }

    public AuthResponse refreshToken(RefreshRequest request) {
        // 1. Valider le refresh token
        String email = jwtService.getEmailFromToken(request.getRefreshToken());

        // 2. Trouver l'utilisateur
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        // 3. Générer un nouveau access token
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.isAdmin());

        // 4. Réutiliser le même refresh token ou en générer un nouveau
        return new AuthResponse(newAccessToken, request.getRefreshToken(), user);
    }

    public void logout(String token) {
        log.info("Utilisateur déconnecté - Token: {}", token);
    }

    public void forgotPassword(String email) {
        // Implémentation basique - envoi d'email à faire
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            log.info("Email de reset envoyé à: {}", email);
        } else {
            log.info("Si l'email existe, un lien de reset a été envoyé");
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        // 1. Valider le token de reset (simplifié)
        // 2. Trouver l'utilisateur
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email incorrect");
        }

        User user = userOpt.get();

        // 3. Hasher le nouveau mot de passe
        String hashedPassword = passwordEncoder.hashPassword(request.getNewPassword());

        // 4. Mettre à jour l'utilisateur
        user.setPassword(hashedPassword);
        userRepository.save(user);

        log.info("Mot de passe mis à jour pour: {}", request.getEmail());
    }
}