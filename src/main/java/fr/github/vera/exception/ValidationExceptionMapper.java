package fr.github.vera.exception;

import fr.github.vera.documention.ErrorResponseApi;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    private static final Logger log = LogManager.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(ValidationException exception) {
        log.warn("Validation error: {}", exception.getMessage());

        ErrorResponseApi errorResponse = new ErrorResponseApi(exception.getMessage());
        return Response.status(exception.getStatusCode())
                .entity(errorResponse)
                .build();
    }
}
