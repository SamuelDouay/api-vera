package fr.github.vera.resources;

import fr.github.vera.model.AuthResponse;
import fr.github.vera.model.LoginRequest;
import fr.github.vera.model.RegisterRequest;
import fr.github.vera.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Gestion de l'authentification")
public class AuthResource {
    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/login")
    @Operation(summary = "Connexion utilisateur")
    public Response login(LoginRequest request) {
        // Authentification email/password
        AuthResponse auth = authService.authenticate(request);
        return Response.ok(auth).build();
    }

    @POST
    @Path("/register")
    @Operation(summary = "Inscription utilisateur")
    public Response register(RegisterRequest request) {
        // Création user + hash password
        AuthResponse auth = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(auth).build();
    }

    @POST
    @Path("/refresh")
    @Operation(summary = "Rafraîchir le token")
    public Response refreshToken(String request) {
        // Générer nouveau token
        AuthResponse auth = authService.refreshToken(request);
        return Response.ok(auth).build();
    }

    @POST
    @Path("/logout")
    @Operation(summary = "Déconnexion")
    public Response logout(@HeaderParam("Authorization") String token) {
        // Invalider le token
        authService.logout(token);
        return Response.noContent().build();
    }

    @POST
    @Path("/reset")
    @Operation(summary = "Réinitialiser mot de passe")
    public Response resetPassword(Object request) {
        // Reset avec token
        authService.resetPassword(request);
        return Response.ok().build();
    }
}