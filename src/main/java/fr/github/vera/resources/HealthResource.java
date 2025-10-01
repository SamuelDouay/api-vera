package fr.github.vera.resources;

import fr.github.vera.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Endpoints de santé de l'application")
public class HealthResource {

    @GET
    @Operation(summary = "Vérifier l'état de santé de l'application")
    public Response healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());

        return Response.ok(health).build();
    }
}