package com.cms.payment.exception;

public class UnavailableException extends PaymentException {
    public UnavailableException(String errorMessage) {
        super(errorMessage);
    }

    public UnavailableException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
