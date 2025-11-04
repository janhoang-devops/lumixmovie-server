package com.project.lumix.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // Nếu ErrorCode có trường message
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return errorCode != null ? errorCode.getMessage() : super.getMessage();
    }
}
