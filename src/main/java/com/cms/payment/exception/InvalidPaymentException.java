package com.cms.payment.exception;

public class InvalidPaymentException extends PaymentException {
    public InvalidPaymentException(String errorMessage) {
        super(errorMessage);
    }

    public InvalidPaymentException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
