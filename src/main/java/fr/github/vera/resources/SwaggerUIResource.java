package fr.github.vera.resources;

import fr.github.vera.filters.Public;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/swagger-ui")
@Public
public class SwaggerUIResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getSwaggerUI() {
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>VERA API - Documentation</title>
                    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.9.0/swagger-ui.css" />
                    <style>
                        html { box-sizing: border-box; overflow-y: scroll; }
                        body { margin: 0; background: #fafafa; }
                    </style>
                </head>
                <body>
                    <div id="swagger-ui"></div>
                    <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.9.0/swagger-ui-bundle.js"></script>
                    <script>
                        window.onload = function() {
                            window.ui = SwaggerUIBundle({
                                url: '/api/openapi.json',
                                dom_id: '#swagger-ui',
                                deepLinking: true,
                                presets: [
                                    SwaggerUIBundle.presets.apis,
                                    SwaggerUIBundle.presets.standalone
                                ],
                                displayRequestDuration: true,
                                filter: true
                            });
                        };
                    </script>
                </body>
                </html>
                """;
        return Response.ok(html).build();
    }
}