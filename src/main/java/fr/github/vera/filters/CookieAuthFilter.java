package fr.github.vera.filters;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION - 5)
public class CookieAuthFilter implements ContainerRequestFilter {

    private static final Logger logger = LogManager.getLogger(CookieAuthFilter.class);
    private static final String AUTH_COOKIE_NAME = "auth_token";
    private static final String AUTH_HEADER = "Authorization";
    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";
    private static final String WEB_CLIENT = "web";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (requestContext.getHeaders().containsKey(AUTH_HEADER)) {
            logger.debug("Header Authorization déjà présent - skip CookieAuthFilter");
            return;
        }

        String clientType = requestContext.getHeaderString(CLIENT_TYPE_HEADER);
        boolean isWebClient = WEB_CLIENT.equalsIgnoreCase(clientType);

        if (!isWebClient) {
            logger.debug("Client non-web - skip CookieAuthFilter");
            return;
        }

        Cookie authCookie = requestContext.getCookies().get(AUTH_COOKIE_NAME);

        if (authCookie != null && authCookie.getValue() != null && !authCookie.getValue().isEmpty()) {
            requestContext.getHeaders().add(AUTH_HEADER, "Bearer " + authCookie.getValue());
            logger.debug("Token extrait du cookie et ajouté au header");
        } else {
            logger.debug("Aucun token trouvé dans les cookies");
        }
    }
}