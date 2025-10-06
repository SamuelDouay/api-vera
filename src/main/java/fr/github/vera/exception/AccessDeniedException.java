package fr.github.vera.exception;

import jakarta.ws.rs.core.Response;

public class AccessDeniedException extends ValidationException {
    public AccessDeniedException(String message) {
        super(message, Response.Status.FORBIDDEN.getStatusCode());
    }
}