package fr.github.vera.resources;

import com.codahale.metrics.MetricRegistry;
import fr.github.vera.services.UserService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    @GET
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

        metrics.getCounters().forEach((name, counter) -> {
            metricsData.put(name, counter.getCount());
        });

        return Response.ok(metricsData).build();
    }
}