package fr.github.vera.exception;

import fr.github.vera.model.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        logger.error("Error caught: {}", exception.getMessage(), exception);

        switch (exception) {
            case NotFoundException _ -> {
                logger.warn("Route not found: {}", exception.getMessage());
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiResponse("Endpoint not found", 404))
                        .build();
            }
            case WebApplicationException webEx -> {
                int status = webEx.getResponse().getStatus();
                String message = getCustomMessageForStatus(status);
                logger.warn("Web application error: {} - {}", status, message);
                return Response.status(status)
                        .entity(new ApiResponse(message, status))
                        .build();
            }
            case ConstraintViolationException _ -> {
                logger.warn("Validation error: {}", exception.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiResponse("Invalid input data", 400))
                        .build();
            }
            default -> {
                logger.error("Unhandled exception: ", exception);
            }
        }

        // Generic error
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse("Internal server error", 500))
                .build();
    }

    private String getCustomMessageForStatus(int status) {
        return switch (status) {
            case 400 -> "Bad request - invalid parameters";
            case 401 -> "Authentication required";
            case 403 -> "Access denied";
            case 404 -> "Resource not found";
            case 405 -> "HTTP method not allowed";
            case 409 -> "Conflict - resource already exists";
            case 415 -> "Unsupported media type";
            case 422 -> "Unprocessable entity";
            case 429 -> "Rate limit exceeded";
            case 500 -> "Internal server error";
            case 503 -> "Service temporarily unavailable";
            default -> "An error occurred";
        };
    }
}