package fr.github.vera.exception;

import jakarta.ws.rs.core.Response;

public class InvalidDataException extends ValidationException {
    public InvalidDataException(String message) {
        super(message, Response.Status.BAD_REQUEST.getStatusCode());
    }
}