package fr.github.vera.exception;

import jakarta.ws.rs.core.Response;

public class UserNotFoundException extends ValidationException {
    public UserNotFoundException(String message) {
        super(message, Response.Status.NOT_FOUND.getStatusCode());
    }
}