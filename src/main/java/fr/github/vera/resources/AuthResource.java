package fr.github.vera.resources;

import fr.github.vera.Main;
import fr.github.vera.filters.Public;
import fr.github.vera.filters.Secured;
import fr.github.vera.model.authentification.*;
import fr.github.vera.response.Response;
import fr.github.vera.security.JwtService;
import fr.github.vera.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication")
public class AuthResource {
    private final AuthService authService = new AuthService();
    private final JwtService jwtService = new JwtService();

    @Context
    private jakarta.ws.rs.container.ContainerRequestContext requestContext;

    @POST
    @Public
    @Path("/login")
    @Operation(summary = "Connexion utilisateur")
    public jakarta.ws.rs.core.Response login(LoginRequest request) {
        return handleAuthResponse(authService.authenticate(request, requestContext));
    }

    @POST
    @Public
    @Path("/register")
    @Operation(summary = "Inscription utilisateur")
    public jakarta.ws.rs.core.Response register(RegisterRequest request) {
        return handleAuthResponse(authService.register(request, requestContext), jakarta.ws.rs.core.Response.Status.CREATED);
    }

    @POST
    @Public
    @Path("/refresh")
    @Operation(summary = "Rafraîchir le token")
    public jakarta.ws.rs.core.Response refreshToken(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        if (refreshToken == null && requestContext.getCookies().containsKey("refresh_token")) {
            refreshToken = requestContext.getCookies().get("refresh_token").getValue();
        }

        if (refreshToken == null) {
            return errorResponse("Refresh token manquant");
        }

        return handleAuthResponse(authService.refreshToken(refreshToken, requestContext));
    }

    @POST
    @Path("/logout")
    @Secured()
    @Operation(summary = "Déconnexion")
    public jakarta.ws.rs.core.Response logout() {
        String jwtToken = extractJwtToken();

        if (jwtToken != null) {
            Main.getTokenBlacklistService().blacklistToken(
                    jwtToken,
                    jwtService.getExpiration(jwtToken),
                    jwtService.getUserId(jwtToken)
            );
        }

        return handleSimpleResponse(authService.logout(jwtToken != null ? jwtToken : "", requestContext));
    }

    @POST
    @Public
    @Path("/forgot")
    @Operation(summary = "Mot de passe oublié")
    public jakarta.ws.rs.core.Response forgotPassword(@QueryParam("email") String email) {
        authService.forgotPassword(email);
        return jakarta.ws.rs.core.Response.ok(new Response<>("Email envoyé si l'utilisateur existe")).build();
    }

    @POST
    @Public
    @Path("/reset")
    @Operation(summary = "Réinitialiser mot de passe")
    public jakarta.ws.rs.core.Response resetPassword(ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return jakarta.ws.rs.core.Response.ok(new Response<>("Mot de passe mis à jour")).build();
        } catch (Exception e) {
            return errorResponse(e.getMessage());
        }
    }

    private jakarta.ws.rs.core.Response handleAuthResponse(jakarta.ws.rs.core.Response serviceResponse) {
        return handleAuthResponse(serviceResponse, jakarta.ws.rs.core.Response.Status.OK);
    }

    private jakarta.ws.rs.core.Response handleAuthResponse(jakarta.ws.rs.core.Response serviceResponse, jakarta.ws.rs.core.Response.Status status) {
        jakarta.ws.rs.core.Response.ResponseBuilder builder = jakarta.ws.rs.core.Response.status(status);

        if (serviceResponse.getEntity() instanceof AuthResponse) {
            builder.entity(new Response<>((AuthResponse) serviceResponse.getEntity()));
        } else {
            builder.entity(new Response<>(serviceResponse.getEntity()));
        }

        serviceResponse.getCookies().values().forEach(builder::cookie);
        return builder.build();
    }

    private jakarta.ws.rs.core.Response handleSimpleResponse(jakarta.ws.rs.core.Response serviceResponse) {
        jakarta.ws.rs.core.Response.ResponseBuilder builder = jakarta.ws.rs.core.Response.ok(
                new Response<>(serviceResponse.getEntity())
        );
        serviceResponse.getCookies().values().forEach(builder::cookie);
        return builder.build();
    }

    private jakarta.ws.rs.core.Response errorResponse(String message) {
        return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.BAD_REQUEST)
                .entity(new Response<>(message))
                .build();
    }

    private String extractJwtToken() {
        String authHeader = ((jakarta.ws.rs.core.HttpHeaders) requestContext).getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return requestContext.getCookies().getOrDefault("auth_token",
                new jakarta.ws.rs.core.Cookie("auth_token", "")).getValue();
    }
}