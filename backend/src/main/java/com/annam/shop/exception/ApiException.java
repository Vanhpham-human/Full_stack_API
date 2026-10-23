package com.annam.shop.exception;

import java.util.List;

public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<FieldIssue> details;

    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage(), List.of());
    }

    public ApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, List.of());
    }

    public ApiException(ErrorCode errorCode, String message, List<FieldIssue> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<FieldIssue> getDetails() {
        return details;
    }
}
