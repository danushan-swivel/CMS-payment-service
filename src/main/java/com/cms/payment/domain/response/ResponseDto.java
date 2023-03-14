package com.cms.payment.domain.response;

import com.cms.payment.exception.PaymentException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ResponseDto {
    public String toLogJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new PaymentException("Convert object to string is failed", e);
        }
    }
}
