package com.project.lumix.exception;

import com.project.lumix.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Objects;

public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler({Exception.class})
    ResponseEntity<ApiResponse> handleException(Exception e) {
        ApiResponse<?> apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler({AppException.class})
    ResponseEntity<ApiResponse> handlingAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<?> apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler({AccessDeniedException.class})
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException e) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getStatusCode()).body(ApiResponse.builder().code(errorCode.getCode()).message(errorCode.getMessage()).build());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    ResponseEntity<ApiResponse> handlingException(MethodArgumentNotValidException ex) {
        String enumKey = ex.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
            ConstraintViolation constraintViolation = (ConstraintViolation) ((ObjectError)ex.getBindingResult().getAllErrors().getFirst()).unwrap(ConstraintViolation.class);
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
        } catch (IllegalArgumentException var6) {
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attributes) ? this.mapAttribute(errorCode.getMessage(), attributes) : errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get("min"));
        return message.replace("{min}", minValue);
    }
}
