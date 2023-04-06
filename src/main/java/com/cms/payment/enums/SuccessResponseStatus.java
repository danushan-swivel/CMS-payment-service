package com.cms.payment.enums;

import lombok.Getter;

@Getter
public enum SuccessResponseStatus {

    PAID_SUCCESSFUL("The payment made successfully"),
    READ_STUDENT("Student retrieved successfully"),
    READ_STUDENT_LIST("Students details retrieved successfully"),
    READ_LOCATION_LIST("All location details retrieved successfully"),
    PAYMENT_UPDATED("The payment updated successfully"),
    PAYMENT_DELETED("The payment deleted successfully"),
    READ_PAYMENT("The payment retrieved successfully"),
    READ_LIST_PAYMENT("The payment list retrieved successfully"),
    READ_STUDENT_LIST_PAYMENT("The student payment list retrieved successfully"),
    READ_STUDENT_PAYMENT_REPORT("The student payment report retrieved successfully");
    private final String message;

    SuccessResponseStatus(String message) {
        this.message = message;
    }
}
