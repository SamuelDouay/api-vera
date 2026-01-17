package fr.github.vera.services;

import fr.github.vera.model.User;
import fr.github.vera.model.authentification.AuthResponse;
import fr.github.vera.model.authentification.LoginRequest;
import fr.github.vera.model.authentification.RegisterRequest;
import fr.github.vera.model.authentification.ResetPasswordRequest;
import fr.github.vera.repository.IUserRepository;
import fr.github.vera.repository.UserRepository;
import fr.github.vera.security.JwtService;
import fr.github.vera.security.PasswordEncoder;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class AuthService {
    private static final Logger log = LogManager.getLogger(AuthService.class);
    private static final String AUTH_COOKIE_NAME = "auth_token";
    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";
    private static final String WEB_CLIENT = "web";
    private static final int COOKIE_MAX_AGE = 3600;
    private static final boolean SECURE_COOKIE = false;
    private final IUserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.jwtService = new JwtService();
        this.passwordEncoder = new PasswordEncoder();
    }

    // 🔥 NOUVELLE méthode qui accepte le contexte
    public Response authenticate(LoginRequest request, jakarta.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            // 1. Trouver l'utilisateur
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new AuthResponse(null, null, null))
                        .build();
            }

            User user = userOpt.get();

            // 2. Vérifier le mot de passe
            if (!passwordEncoder.verifyPassword(request.getPassword(), user.getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new AuthResponse(null, null, null))
                        .build();
            }

            // 3. Générer les tokens
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.isAdmin());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            // 4. Créer la réponse
            AuthResponse authResponse = new AuthResponse(accessToken, refreshToken, user);
            Response.ResponseBuilder responseBuilder = Response.ok(authResponse);

            // 5. Ajouter les cookies SEULEMENT pour les clients web
            if (isWebClient(requestContext)) {
                log.info("Client web détecté - ajout des cookies HttpOnly");
                NewCookie authCookie = createAuthCookie(accessToken);
                NewCookie refreshCookie = createRefreshCookie(refreshToken);
                responseBuilder.cookie(authCookie, refreshCookie);
            } else {
                log.info("Client API détecté (Postman/curl) - pas de cookies");
            }

            return responseBuilder.build();

        } catch (Exception e) {
            log.error("Erreur lors de l'authentification", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new AuthResponse(null, null, null))
                    .build();
        }
    }

    // 🔥 Autres méthodes aussi avec contexte
    public Response register(RegisterRequest request, jakarta.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            // 1. Vérifier si l'email existe déjà
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new AuthResponse(null, null, null))
                        .build();
            }

            // 2. Créer l'utilisateur
            User newUser = new User();
            newUser.setName(request.getName());
            newUser.setSurname(request.getSurname());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.hashPassword(request.getPassword()));
            newUser.setAdmin(false);

            // 3. Sauvegarder
            User savedUser = userRepository.save(newUser);

            // 4. Générer les tokens
            String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.isAdmin());
            String refreshToken = jwtService.generateRefreshToken(savedUser.getEmail());

            // 5. Créer la réponse
            AuthResponse authResponse = new AuthResponse(accessToken, refreshToken, savedUser);
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.CREATED).entity(authResponse);

            // 6. Ajouter les cookies pour les clients web
            if (isWebClient(requestContext)) {
                NewCookie authCookie = createAuthCookie(accessToken);
                NewCookie refreshCookie = createRefreshCookie(refreshToken);
                responseBuilder.cookie(authCookie, refreshCookie);
            }

            return responseBuilder.build();

        } catch (Exception e) {
            log.error("Erreur lors de l'inscription", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new AuthResponse(null, null, null))
                    .build();
        }
    }

    public Response refreshToken(String refreshTokenFromCookie, jakarta.ws.rs.container.ContainerRequestContext requestContext) {
        try {
            // 1. Valider le refresh token
            String email = jwtService.getEmailFromToken(refreshTokenFromCookie);

            // 2. Trouver l'utilisateur
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Utilisateur non trouvé\"}")
                        .build();
            }

            User user = userOpt.get();

            // 3. Générer un nouveau access token
            String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.isAdmin());

            // 4. Créer la réponse
            AuthResponse authResponse = new AuthResponse(newAccessToken, refreshTokenFromCookie, user);
            Response.ResponseBuilder responseBuilder = Response.ok(authResponse);

            // 5. Ajouter le cookie pour les clients web
            if (isWebClient(requestContext)) {
                NewCookie authCookie = createAuthCookie(newAccessToken);
                responseBuilder.cookie(authCookie);
            }

            return responseBuilder.build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Refresh token invalide\"}")
                    .build();
        }
    }

    public Response logout(String token, jakarta.ws.rs.container.ContainerRequestContext requestContext) {
        // 1. Créer la réponse
        Response.ResponseBuilder responseBuilder = Response.ok()
                .entity("{\"message\": \"Logout successful\"}");

        // 2. Supprimer les cookies SEULEMENT pour les clients web
        if (isWebClient(requestContext)) {
            NewCookie deleteAuthCookie = deleteCookie(AUTH_COOKIE_NAME);
            NewCookie deleteRefreshCookie = deleteCookie(REFRESH_COOKIE_NAME);
            responseBuilder.cookie(deleteAuthCookie, deleteRefreshCookie);
        }

        log.info("Utilisateur déconnecté");
        return responseBuilder.build();
    }

    // 🔥 Méthode utilitaire avec contexte passé en paramètre
    private boolean isWebClient(jakarta.ws.rs.container.ContainerRequestContext requestContext) {
        if (requestContext == null) {
            log.warn("RequestContext est null - considéré comme client API");
            return false;
        }

        String clientType = requestContext.getHeaderString(CLIENT_TYPE_HEADER);
        boolean isWeb = WEB_CLIENT.equalsIgnoreCase(clientType);

        if (isWeb) {
            log.debug("Client web détecté (X-Client-Type: web)");
        } else {
            log.debug("Client API détecté (Postman/curl/mobile) - header: {}", clientType);
        }

        return isWeb;
    }

    // Méthodes de création de cookies (inchangées)
    private NewCookie createAuthCookie(String token) {
        return new NewCookie.Builder(AUTH_COOKIE_NAME)
                .value(token)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .secure(SECURE_COOKIE)
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }

    private NewCookie createRefreshCookie(String token) {
        return new NewCookie.Builder(REFRESH_COOKIE_NAME)
                .value(token)
                .path("/")
                .maxAge(7 * 24 * 3600)
                .secure(SECURE_COOKIE)
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }

    private NewCookie deleteCookie(String name) {
        return new NewCookie.Builder(name)
                .value("")
                .path("/")
                .maxAge(0)
                .build();
    }

    // Les méthodes qui n'ont pas besoin de cookies (inchangées)
    public void forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            log.info("Email de reset envoyé à: {}", email);
        } else {
            log.info("Si l'email existe, un lien de reset a été envoyé");
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email incorrect");
        }

        User user = userOpt.get();
        String hashedPassword = passwordEncoder.hashPassword(request.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        log.info("Mot de passe mis à jour pour: {}", request.getEmail());
    }
}