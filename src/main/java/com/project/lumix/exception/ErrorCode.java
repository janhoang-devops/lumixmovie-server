package com.project.lumix.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_EXISTED(1001, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1015, "User not exists", HttpStatus.BAD_REQUEST),
    USER_INVALID(1002, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),
    USER_PASSWORD_INVALID(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),

    // Auth
    UNAUTHORIZED(1005, "Unauthorized", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_VERIFIED(1007, "Account has not been verified", HttpStatus.FORBIDDEN),

    // Token
    TOKEN_EXPIRED(1008, "Token is expired", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1009, "Token is invalid", HttpStatus.BAD_REQUEST),
    INVALID_KEY(1015, "Invalid key", HttpStatus.BAD_REQUEST),
    // Movie
    MOVIE_NOT_EXISTED(2001, "Movie not existed", HttpStatus.NOT_FOUND),
    MOVIE_EXISTED(2002, "Movie existed", HttpStatus.BAD_REQUEST),
    MOVIE_NOT_FOUND(2003, "Not found movie", HttpStatus.NOT_FOUND),

    // Comment
    COMMENT_NOT_FOUND(1012, "Comment not found", HttpStatus.NOT_FOUND),

    // Email
    EMAIL_EXISTED(1013, "Email already exists", HttpStatus.BAD_REQUEST),

    // Role
    ROLE_NOT_FOUND(1014, "Role not found", HttpStatus.NOT_FOUND),

    // Genre
    GENRE_NOT_FOUND(1016, "Genre not found", HttpStatus.NOT_FOUND),
    GENRE_EXISTED(1017, "Genre existed", HttpStatus.BAD_REQUEST),

    // ====== SERVER / SYSTEM ======
    INTERNAL_SERVER_ERROR(5000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(5001, "Database error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    ;
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
