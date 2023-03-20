package com.cms.payment.exception;

public class ConnectionException extends PaymentException {
    public ConnectionException(String errorMessage) {
        super(errorMessage);
    }

    public ConnectionException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
