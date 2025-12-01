package fr.github.vera.exception;

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

        ErrorResponse errorResponse = createErrorResponse(exception);
        return Response.status(errorResponse.getStatus())
                .entity(new fr.github.vera.response.Response<>(errorResponse.getMessage()))
                .build();
    }

    private ErrorResponse createErrorResponse(Exception exception) {
        return switch (exception) {
            case ValidationException vaEx -> new ErrorResponse(vaEx.getStatusCode(), exception.getMessage());

            case NotFoundException e -> {
                logger.warn("Route not found: {}", e.getMessage());
                yield new ErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), "Endpoint not found");
            }

            case ConstraintViolationException e -> {
                logger.warn("Validation error: {}", e.getMessage());
                yield new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid input data");
            }

            case WebApplicationException webEx -> {
                int status = webEx.getResponse().getStatus();
                String message = getCustomMessageForStatus(status, exception.getMessage());
                logger.warn("Web application error: {} - {}", status, message);
                yield new ErrorResponse(status, message);
            }

            default -> {
                logger.error("Unhandled exception: ", exception);
                yield new ErrorResponse(
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        "Internal server error"
                );
            }
        };
    }

    private String getCustomMessageForStatus(int status, String defaultMessage) {
        String message = switch (status) {
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

        // Utilise le message par défaut si plus spécifique
        return defaultMessage != null && !defaultMessage.trim().isEmpty() ?
                defaultMessage : message;
    }

    // Record pour encapsuler la réponse d'erreur
    private record ErrorResponse(int status, String message) {
        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}