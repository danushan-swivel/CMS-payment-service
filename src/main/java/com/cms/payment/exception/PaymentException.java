package com.cms.payment.exception;

public class PaymentException extends RuntimeException{
    public PaymentException(String errorMessage) {
        super(errorMessage);
    }

    public PaymentException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
