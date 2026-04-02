package com.chtrembl.petstoreapp.exception;

public class PetServiceException extends RuntimeException {
    public PetServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}