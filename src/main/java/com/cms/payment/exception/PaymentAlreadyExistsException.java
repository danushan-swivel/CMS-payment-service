package com.cms.payment.exception;

public class PaymentAlreadyExistsException extends PaymentException {
    public PaymentAlreadyExistsException(String errorMessage) {
        super(errorMessage);
    }

    public PaymentAlreadyExistsException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
