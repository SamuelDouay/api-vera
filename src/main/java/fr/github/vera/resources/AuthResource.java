package fr.github.vera.resources;

import fr.github.vera.Main;
import fr.github.vera.filters.Public;
import fr.github.vera.filters.Secured;
import fr.github.vera.model.LoginRequest;
import fr.github.vera.model.RefreshRequest;
import fr.github.vera.model.RegisterRequest;
import fr.github.vera.model.ResetPasswordRequest;
import fr.github.vera.response.AuthResponse;
import fr.github.vera.response.ErrorResponse;
import fr.github.vera.security.JwtService;
import fr.github.vera.services.AuthService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Date;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Gestion de l'authentification")
public class AuthResource {
    private final AuthService authService = new AuthService();
    private final JwtService jwtService = new JwtService();

    @POST
    @Public
    @Path("/login")
    @Operation(summary = "Connexion utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connexion réussie"),
            @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    })
    public Response login(LoginRequest request) {
        try {
            fr.github.vera.model.AuthResponse auth = authService.authenticate(request);
            AuthResponse response = new AuthResponse(auth);
            return Response.ok(response).build();
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse).build();
        }
    }

    @POST
    @Public
    @Path("/register")
    @Operation(summary = "Inscription utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
            @ApiResponse(responseCode = "400", description = "Email déjà utilisé")
    })
    public Response register(RegisterRequest request) {
        try {
            fr.github.vera.model.AuthResponse auth = authService.register(request);
            AuthResponse response = new AuthResponse(auth);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }
    }

    @POST
    @Path("/refresh")
    @Operation(summary = "Rafraîchir le token")
    public Response refreshToken(RefreshRequest request) {
        try {
            fr.github.vera.model.AuthResponse auth = authService.refreshToken(request);
            AuthResponse response = new AuthResponse(auth);
            return Response.ok(response).build();
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse).build();
        }
    }

    @POST
    @Path("/logout")
    @Secured()
    @Operation(summary = "Déconnexion")
    public Response logout(@HeaderParam("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);

            Integer userId = extractUserIdFromToken(jwtToken);
            Date date = extractExpirationFromToken(jwtToken);

            var blacklistService = Main.getTokenBlacklistService();
            if (blacklistService != null)
                blacklistService.blacklistToken(jwtToken, date, userId);

            authService.logout(jwtToken);
        }
        return Response.noContent().build();
    }

    private Integer extractUserIdFromToken(String token) {
        Claims claims = jwtService.validateToken(token);
        return claims.get("userId", Integer.class);
    }

    private java.util.Date extractExpirationFromToken(String token) {
        Claims claims = jwtService.validateToken(token);
        return claims.getExpiration();
    }


    @POST
    @Public
    @Path("/forgot")
    @Operation(summary = "Mot de passe oublié")
    public Response forgotPassword(@QueryParam("email") String email) {
        authService.forgotPassword(email);
        ErrorResponse response = new ErrorResponse("Si l'email existe, un lien de reset a été envoyé");
        return Response.ok(response).build();
    }

    @POST
    @Public
    @Path("/reset")
    @Operation(summary = "Réinitialiser mot de passe")
    public Response resetPassword(ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            ErrorResponse response = new ErrorResponse("Mot de passe mis à jour avec succès");
            return Response.ok(response).build();
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }
    }
}