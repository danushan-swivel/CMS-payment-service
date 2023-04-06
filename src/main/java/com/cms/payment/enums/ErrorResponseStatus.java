package com.cms.payment.enums;

import lombok.Getter;

@Getter
public enum ErrorResponseStatus {
    INTERNAL_SERVER_ERROR("Internal server error"),
    INTER_CONNECTION_FAILED("Internal server connection error"),
    MISSING_REQUIRED_FIELDS("The required fields are missing"),
    INVALID_STUDENT("The student Id is invalid"),
    INVALID_PAYMENT("The payment Id is invalid"),
    ALREADY_PAID("The payment already made for specific month");
    private final String message;

    ErrorResponseStatus(String message) {
        this.message = message;
    }
}
