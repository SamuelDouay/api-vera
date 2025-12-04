package fr.github.vera.resources;

import com.codahale.metrics.MetricRegistry;
import fr.github.vera.filters.Secured;
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

@Path("/admin")
@Tag(name = "Admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    @GET
    @Path("/metric")
    @Secured(adminOnly = true)
    public Response getMetrics() {
        MetricRegistry metrics = new MetricRegistry();
        Map<String, Object> metricsData = new HashMap<>();

        metrics.getTimers().forEach((name, timer) -> {
            Map<String, Object> timerData = new HashMap<>();
            timerData.put("count", 0);
            timerData.put("meanRate", 0);
            timerData.put("mean", 0);
            metricsData.put(name, timerData);
        });

        metrics.getCounters().forEach((name, counter) ->
                metricsData.put(name, counter.getCount())
        );

        return Response.ok(metricsData).build();
    }

    @GET
    @Path("/health")
    @Secured(adminOnly = true)
    @Operation(summary = "Vérifier l'état de santé de l'application")
    public Response healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());

        return Response.ok(health).build();
    }
}
