package fr.github.vera.filters;

import fr.github.vera.config.ConfigProperties;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final String ALLOWED_DOMAIN_KEY = "cors.allowed-domain";
    private static final String IS_PUBLIC = "isPublic";
    private static final String ORIGIN = "Origin";
    private static final String REFERER = "Referer";
    private static final String OPTIONS = "OPTIONS";

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        if (OPTIONS.equals(request.getMethod())) {
            handlePreflight(request);
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        addCorsHeaders(request, response);
    }

    private void handlePreflight(ContainerRequestContext request) {
        String origin = request.getHeaderString(ORIGIN);
        String domain = getAllowedDomain();

        Response.ResponseBuilder response = Response.ok();
        addCorsHeaders(response);

        if (isDomainAllowed(origin, domain)) {
            response.header("Access-Control-Allow-Origin", origin)
                    .header("Access-Control-Allow-Credentials", "true");
        }

        request.abortWith(response.build());
    }

    private void addCorsHeaders(ContainerRequestContext request, ContainerResponseContext response) {
        String origin = request.getHeaderString(ORIGIN);
        String referer = request.getHeaderString(REFERER);
        String domain = getAllowedDomain();

        if (isPublicEndpoint(request)) {
            response.getHeaders().add("Access-Control-Allow-Origin", "*");
        } else if (isDomainAllowed(origin, domain)) {
            response.getHeaders().add("Access-Control-Allow-Origin", origin);
            response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        } else if (isDomainAllowed(referer, domain)) {
            response.getHeaders().add("Access-Control-Allow-Origin", extractDomain(referer));
            response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        } else {
            blockRequest(response);
        }

        addStandardHeaders(response);
    }

    private void addCorsHeaders(Response.ResponseBuilder response) {
        response.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-requested-with, x-csrf-token")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH")
                .header("Access-Control-Max-Age", "3600")
                .header("Access-Control-Expose-Headers", "authorization");
    }

    private void addStandardHeaders(ContainerResponseContext response) {
        response.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-requested-with, x-csrf-token");
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        response.getHeaders().add("Access-Control-Max-Age", "3600");
        response.getHeaders().add("Access-Control-Expose-Headers", "authorization");
    }

    private boolean isDomainAllowed(String source, String allowedDomain) {
        if (source == null || source.isEmpty() || allowedDomain == null) return false;

        String[] domains = allowedDomain.split(",");
        for (String domain : domains) {
            String trimmed = domain.trim();
            if (source.contains(trimmed) || source.contains("localhost")) {
                return true;
            }
        }
        return false;
    }

    private String extractDomain(String url) {
        if (url == null) return "";
        int pathStart = url.indexOf("/", url.indexOf("://") + 3);
        return pathStart > 0 ? url.substring(0, pathStart) : url;
    }

    private boolean isPublicEndpoint(ContainerRequestContext request) {
        Object property = request.getProperty(IS_PUBLIC);
        return property != null &&
                (Boolean.TRUE.equals(property) || "true".equals(property.toString()));
    }

    private String getAllowedDomain() {
        return ConfigProperties.getInstance().getProperty(ALLOWED_DOMAIN_KEY);
    }

    private void blockRequest(ContainerResponseContext response) {
        response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
        response.setEntity("{\"error\": \"CORS policy: Origin not allowed\"}");
        response.getHeaders().putSingle("Content-Type", "application/json");
    }
}