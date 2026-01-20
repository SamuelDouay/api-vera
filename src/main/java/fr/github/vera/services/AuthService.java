package fr.github.vera.services;

import fr.github.vera.model.User;
import fr.github.vera.model.authentification.AuthResponse;
import fr.github.vera.model.authentification.LoginRequest;
import fr.github.vera.model.authentification.RegisterRequest;
import fr.github.vera.model.authentification.ResetPasswordRequest;
import fr.github.vera.repository.UserRepository;
import fr.github.vera.security.JwtService;
import fr.github.vera.security.PasswordEncoder;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthService {
    private static final Logger log = LogManager.getLogger(AuthService.class);
    private final UserRepository userRepository = new UserRepository();
    private final JwtService jwtService = new JwtService();
    private final PasswordEncoder passwordEncoder = new PasswordEncoder();

    public Response authenticate(LoginRequest request, jakarta.ws.rs.container.ContainerRequestContext context) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .filter(u -> passwordEncoder.verifyPassword(request.getPassword(), u.getPassword()))
                    .orElseThrow(() -> new RuntimeException("Identifiants invalides"));

            return createAuthResponse(user, context);
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Identifiants invalides").build();
        }
    }

    public Response register(RegisterRequest request, jakarta.ws.rs.container.ContainerRequestContext context) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Email déjà utilisé").build();
            }

            User user = new User();
            user.setName(request.getName());
            user.setSurname(request.getSurname());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.hashPassword(request.getPassword()));

            User savedUser = userRepository.save(user);
            return createAuthResponse(savedUser, context);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erreur d'inscription").build();
        }
    }

    public Response refreshToken(String refreshToken, jakarta.ws.rs.container.ContainerRequestContext context) {
        try {
            String email = jwtService.getEmailFromToken(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.isAdmin());
            AuthResponse authResponse = new AuthResponse(newAccessToken, refreshToken, user);

            Response.ResponseBuilder builder = Response.ok(authResponse);
            if (isWebClient(context)) {
                builder.cookie(createCookie("auth_token", newAccessToken, 3600));
            }

            return builder.build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Refresh token invalide").build();
        }
    }

    public Response logout(String token, jakarta.ws.rs.container.ContainerRequestContext context) {
        Response.ResponseBuilder builder = Response.ok("Déconnecté");
        if (isWebClient(context)) {
            builder.cookie(createCookie("auth_token", "", 0))
                    .cookie(createCookie("refresh_token", "", 0));
        }
        return builder.build();
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user ->
                log.info("Email de reset envoyé à: {}", email)
        );
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email incorrect"));

        user.setPassword(passwordEncoder.hashPassword(request.getNewPassword()));
        userRepository.save(user);
    }

    private Response createAuthResponse(User user, jakarta.ws.rs.container.ContainerRequestContext context) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.isAdmin());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken, user);

        Response.ResponseBuilder builder = Response.ok(authResponse);
        if (isWebClient(context)) {
            builder.cookie(createCookie("auth_token", accessToken, 3600))
                    .cookie(createCookie("refresh_token", refreshToken, 7 * 24 * 3600));
        }

        return builder.build();
    }

    private NewCookie createCookie(String name, String value, int maxAge) {
        return new NewCookie.Builder(name)
                .value(value)
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }

    private boolean isWebClient(jakarta.ws.rs.container.ContainerRequestContext context) {
        return context != null && "web".equalsIgnoreCase(context.getHeaderString("X-Client-Type"));
    }
}