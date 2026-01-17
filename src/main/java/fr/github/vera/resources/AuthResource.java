package fr.github.vera.resources;

import fr.github.vera.Main;
import fr.github.vera.filters.Public;
import fr.github.vera.filters.Secured;
import fr.github.vera.model.authentification.*;
import fr.github.vera.response.Response;
import fr.github.vera.security.JwtService;
import fr.github.vera.services.AuthService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;

import java.util.Date;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Gestion de l'authentification")
public class AuthResource {
    private final AuthService authService = new AuthService();
    private final JwtService jwtService = new JwtService();

    @Context
    private jakarta.ws.rs.container.ContainerRequestContext requestContext;

    @POST
    @Public
    @Path("/login")
    @Operation(summary = "Connexion utilisateur")
    @ApiResponse(responseCode = "200", description = "Connexion réussie")
    @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    public jakarta.ws.rs.core.Response login(LoginRequest request) {
        try {
            // 🔥 Passer le requestContext au service
            jakarta.ws.rs.core.Response serviceResponse = authService.authenticate(request, requestContext);

            // Extraire l'entité de la réponse du service
            Object entity = serviceResponse.getEntity();

            // Construire la réponse wrapper
            jakarta.ws.rs.core.Response.ResponseBuilder responseBuilder;

            if (entity instanceof AuthResponse) {
                // Si c'est un AuthResponse, le wrapper
                Response<AuthResponse> wrappedResponse = new Response<>((AuthResponse) entity);
                responseBuilder = jakarta.ws.rs.core.Response.ok(wrappedResponse);
            } else {
                // Sinon, utiliser directement
                responseBuilder = jakarta.ws.rs.core.Response.ok(entity);
            }

            // Copier les cookies de la réponse du service
            if (serviceResponse.getCookies() != null) {
                for (NewCookie cookie : serviceResponse.getCookies().values()) {
                    responseBuilder.cookie(cookie);
                }
            }

            return responseBuilder.build();

        } catch (Exception e) {
            Response<String> errorResponse = new Response<>(e.getMessage());
            return jakarta.ws.rs.core.Response
                    .status(jakarta.ws.rs.core.Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .build();
        }
    }

    @POST
    @Public
    @Path("/register")
    @Operation(summary = "Inscription utilisateur")
    @ApiResponse(responseCode = "201", description = "Utilisateur créé")
    @ApiResponse(responseCode = "400", description = "Email déjà utilisé")
    public jakarta.ws.rs.core.Response register(RegisterRequest request) {
        try {
            // 🔥 Passer le requestContext au service
            jakarta.ws.rs.core.Response serviceResponse = authService.register(request, requestContext);

            // Extraire l'entité
            Object entity = serviceResponse.getEntity();

            // Construire la réponse wrapper
            jakarta.ws.rs.core.Response.ResponseBuilder responseBuilder;

            if (entity instanceof AuthResponse) {
                Response<AuthResponse> wrappedResponse = new Response<>((AuthResponse) entity);
                responseBuilder = jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.CREATED).entity(wrappedResponse);
            } else {
                responseBuilder = jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.CREATED).entity(entity);
            }

            // Copier les cookies
            if (serviceResponse.getCookies() != null) {
                for (NewCookie cookie : serviceResponse.getCookies().values()) {
                    responseBuilder.cookie(cookie);
                }
            }

            return responseBuilder.build();

        } catch (Exception e) {
            Response<String> errorResponse = new Response<>(e.getMessage());
            return jakarta.ws.rs.core.Response
                    .status(jakarta.ws.rs.core.Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }

    @POST
    @Public
    @Path("/refresh")
    @Operation(summary = "Rafraîchir le token")
    public jakarta.ws.rs.core.Response refreshToken(RefreshRequest request) {
        try {
            // Pour refresh, on peut utiliser soit le body, soit le cookie
            String refreshToken = request.getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                // Essayer de récupérer depuis les cookies
                if (requestContext != null && requestContext.getCookies().containsKey("refresh_token")) {
                    refreshToken = requestContext.getCookies().get("refresh_token").getValue();
                }
            }

            if (refreshToken == null || refreshToken.isEmpty()) {
                Response<String> errorResponse = new Response<>("Refresh token manquant");
                return jakarta.ws.rs.core.Response
                        .status(jakarta.ws.rs.core.Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            // 🔥 Passer le requestContext au service
            jakarta.ws.rs.core.Response serviceResponse = authService.refreshToken(refreshToken, requestContext);

            // Construire la réponse wrapper
            Object entity = serviceResponse.getEntity();
            jakarta.ws.rs.core.Response.ResponseBuilder responseBuilder;

            if (entity instanceof AuthResponse) {
                Response<AuthResponse> wrappedResponse = new Response<>((AuthResponse) entity);
                responseBuilder = jakarta.ws.rs.core.Response.ok(wrappedResponse);
            } else {
                responseBuilder = jakarta.ws.rs.core.Response.ok(entity);
            }

            // Copier les cookies
            if (serviceResponse.getCookies() != null) {
                for (NewCookie cookie : serviceResponse.getCookies().values()) {
                    responseBuilder.cookie(cookie);
                }
            }

            return responseBuilder.build();

        } catch (Exception e) {
            Response<String> errorResponse = new Response<>(e.getMessage());
            return jakarta.ws.rs.core.Response
                    .status(jakarta.ws.rs.core.Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .build();
        }
    }

    @POST
    @Path("/logout")
    @Secured()
    @Operation(summary = "Déconnexion")
    public jakarta.ws.rs.core.Response logout(@HeaderParam("Authorization") String token) {
        String jwtToken = null;

        // Essayer de récupérer le token depuis le header
        if (token != null && token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }
        // Sinon, essayer depuis les cookies si c'est un client web
        else if (requestContext != null && requestContext.getCookies().containsKey("auth_token")) {
            jwtToken = requestContext.getCookies().get("auth_token").getValue();
        }

        if (jwtToken != null) {
            // Blacklister le token
            Integer userId = extractUserIdFromToken(jwtToken);
            Date date = extractExpirationFromToken(jwtToken);

            var blacklistService = Main.getTokenBlacklistService();
            if (blacklistService != null)
                blacklistService.blacklistToken(jwtToken, date, userId);
        }

        // 🔥 Passer le requestContext au service
        jakarta.ws.rs.core.Response serviceResponse = authService.logout(jwtToken != null ? jwtToken : "", requestContext);

        // Construire la réponse wrapper
        Object entity = serviceResponse.getEntity();
        jakarta.ws.rs.core.Response.ResponseBuilder responseBuilder;

        if (entity instanceof String) {
            Response<String> wrappedResponse = new Response<>((String) entity);
            responseBuilder = jakarta.ws.rs.core.Response.ok(wrappedResponse);
        } else {
            responseBuilder = jakarta.ws.rs.core.Response.ok(entity);
        }

        // Copier les cookies (pour supprimer les cookies HttpOnly)
        if (serviceResponse.getCookies() != null) {
            for (NewCookie cookie : serviceResponse.getCookies().values()) {
                responseBuilder.cookie(cookie);
            }
        }

        return responseBuilder.build();
    }

    private Integer extractUserIdFromToken(String token) {
        try {
            Claims claims = jwtService.validateToken(token);
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }

    private Date extractExpirationFromToken(String token) {
        try {
            Claims claims = jwtService.validateToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            return new Date(System.currentTimeMillis() + 3600000);
        }
    }

    @POST
    @Public
    @Path("/forgot")
    @Operation(summary = "Mot de passe oublié")
    public jakarta.ws.rs.core.Response forgotPassword(@QueryParam("email") String email) {
        authService.forgotPassword(email);
        Response<String> response = new Response<>("Si l'email existe, un lien de reset a été envoyé");
        return jakarta.ws.rs.core.Response.ok(response).build();
    }

    @POST
    @Public
    @Path("/reset")
    @Operation(summary = "Réinitialiser mot de passe")
    public jakarta.ws.rs.core.Response resetPassword(ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            Response<String> response = new Response<>("Mot de passe mis à jour avec succès");
            return jakarta.ws.rs.core.Response.ok(response).build();
        } catch (Exception e) {
            Response<String> errorResponse = new Response<>(e.getMessage());
            return jakarta.ws.rs.core.Response
                    .status(jakarta.ws.rs.core.Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }
}