package fr.github.vera.services;

import fr.github.vera.database.repository.IUserRepository;
import fr.github.vera.database.repository.UserRepository;
import fr.github.vera.model.*;
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
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        log.info(userOpt);
        User user = userOpt.get();
        log.info(user);
        log.info(user.getPassword());

        // 2. Vérifier le mot de passe
        if (!passwordEncoder.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        // 3. Générer les tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
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
        newUser.setRole("user"); // Rôle par défaut

        // 4. Sauvegarder l'utilisateur
        User savedUser = userRepository.save(newUser);

        // 5. Générer les tokens
        String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
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
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        // 4. Réutiliser le même refresh token ou en générer un nouveau
        return new AuthResponse(newAccessToken, request.getRefreshToken(), user);
    }

    public void logout(String token) {
        // Pour un système stateless JWT, on ne fait rien côté serveur
        // Le token sera simplement ignoré côté client
        // Pour un système plus avancé, on pourrait blacklister le token
        System.out.println("Utilisateur déconnecté - Token: " + token.substring(0, 20) + "...");
    }

    public void forgotPassword(String email) {
        // Implémentation basique - envoi d'email à faire
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            // Générer un token de reset et envoyer un email
            System.out.println("Email de reset envoyé à: " + email);
        } else {
            // Ne pas révéler que l'email n'existe pas
            System.out.println("Si l'email existe, un lien de reset a été envoyé");
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        // 1. Valider le token de reset (simplifié)
        // 2. Trouver l'utilisateur
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        // 3. Hasher le nouveau mot de passe
        String hashedPassword = passwordEncoder.hashPassword(request.getNewPassword());

        // 4. Mettre à jour l'utilisateur
        user.setPassword(hashedPassword);
        userRepository.save(user);

        System.out.println("Mot de passe mis à jour pour: " + request.getEmail());
    }
}