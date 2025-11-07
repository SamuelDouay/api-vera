package fr.github.vera.resources;

import fr.github.vera.filters.Secured;
import fr.github.vera.response.CountResponse;
import fr.github.vera.response.Response;
import fr.github.vera.response.SurveyListResponse;
import fr.github.vera.response.SurveyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/survey")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Survey", description = "Gestion des questionnaires")
public class SurveyResource {

    @GET
    @Secured(adminOnly = true)
    @Operation(
            summary = "Récupérer tous les questionaires",
            description = "Retourne la liste de tous les questionaires")
    @ApiResponse(
            responseCode = "200",
            description = "Questionnaires retrieved successfully",
            content = @Content(schema = @Schema(implementation = SurveyListResponse.class))
    )
    public jakarta.ws.rs.core.Response getAllSurvey(@QueryParam("limit") @DefaultValue("100") int limit,
                                                    @QueryParam("offset") @DefaultValue("0") int offset) {
        return null;
    }

    @GET
    @Path("/{id}")
    @Secured()
    @Operation(
            summary = "Récupérer un questionnaire par ID",
            description = "Retourne un questionnaire spécifique par son ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "questionnaire retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "questionnaire not found",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            )
    })
    public jakarta.ws.rs.core.Response getSurveyById(@PathParam("id") Integer id, @Context SecurityContext securityContext) {
        return null;
    }

    @PUT
    @Path("/{id}")
    @Secured()
    @Operation(
            summary = "Mettre à jour un survey",
            description = "Met à jour un survey existant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "survey updated successfully",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "survey not found",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            )
    })
    public jakarta.ws.rs.core.Response updateSurveyById(@PathParam("id") Integer id, @Context SecurityContext securityContext) {
        return null;
    }

    @DELETE
    @Path("/{id}")
    @Secured(adminOnly = true)
    @Operation(
            summary = "Supprimer un survey",
            description = "Supprime un survey par son ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "survey deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "survey not found",
                    content = @Content(schema = @Schema(implementation = Response.class))
            )
    })
    public jakarta.ws.rs.core.Response deleteSurvey(@PathParam("id") Integer id) {
        return null;
    }

    @GET
    @Path("/count")
    @Secured(adminOnly = true)
    @Operation(
            summary = "Récupérer le nombre total survey",
            description = "Retourne le nombre total de survey"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "return survey size",
                    content = @Content(schema = @Schema(implementation = CountResponse.class))
            )
    })
    public jakarta.ws.rs.core.Response count() {
        return null;
    }

    @POST
    @Secured(adminOnly = true)
    @Operation(
            summary = "Créer un nouvel survey",
            description = "Crée un nouvel survey avec les données fournies"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "survey created successfully",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid survey data",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))
            )
    })
    public jakarta.ws.rs.core.Response createUser() {
        return null;
    }
}
