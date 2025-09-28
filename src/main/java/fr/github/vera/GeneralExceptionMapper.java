package fr.github.vera;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Provider
class GeneralExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = LogManager.getLogger(GeneralExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error("Unhandled exception: {}", exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Internal server error", "message", "An unexpected error occurred"))
                .build();
    }
}
