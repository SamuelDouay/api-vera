package fr.github.vera.filters;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        // Accepter toutes les origines
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization, x-requested-with");
        // NE PAS mettre credentials à true quand origin est "*"
        responseContext.getHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Gérer les requêtes OPTIONS (preflight)
        if ("OPTIONS".equals(requestContext.getMethod())) {
            Response response = Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers",
                            "origin, content-type, accept, authorization, x-requested-with")
                    .header("Access-Control-Allow-Methods",
                            "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .header("Access-Control-Max-Age", "3600")
                    .build();
            requestContext.abortWith(response);
        }
    }
}