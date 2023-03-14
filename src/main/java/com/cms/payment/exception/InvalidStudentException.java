package com.cms.payment.exception;

public class InvalidStudentException extends PaymentException {
    public InvalidStudentException(String errorMessage) {
        super(errorMessage);
    }

    public InvalidStudentException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
