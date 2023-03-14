package com.cms.payment.enums;

import lombok.Getter;

@Getter
public enum ErrorResponseStatus {
    INTERNAL_SERVER_ERROR(5000, "Internal server error"),
    MISSING_REQUIRED_FIELDS(4000, "The required fields are missing"),
    INVALID_STUDENT(4001, "The student Id is invalid"),
    INVALID_PAYMENT(4002, "The payment Id is invalid"),
    ALREADY_PAID(4003, "The payment already made for specific month");
    private final String message;
    private final int statusCode;

    ErrorResponseStatus(int statusCode, String message) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
