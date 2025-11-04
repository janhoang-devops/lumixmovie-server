package com.project.lumix.exception;

public class CloudinaryException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public CloudinaryException(String message) {
        super(message);
    }

    public CloudinaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
