package com.cms.payment.enums;

import lombok.Getter;

@Getter
public enum SuccessResponseStatus {

    PAID_SUCCESSFUL(2050, "The payment made successfully"),
    PAYMENT_UPDATED(2051, "The payment updated successfully"),
    PAYMENT_DELETED(2052, "The payment deleted successfully"),
    READ_PAYMENT(2053, "The payment retrieved successfully"),
    READ_LIST_PAYMENT(2054, "The payment list retrieved successfully"),
    READ_STUDENT_LIST_PAYMENT(2055, "The student payment list retrieved successfully"),
    READ_STUDENT_PAYMENT_REPORT(2056, "The student payment report retrieved successfully");
    private final String message;
    private final int statusCode;

    SuccessResponseStatus(int statusCode, String message) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
