package com.cms.payment.domain.request;

public abstract class RequestDto {
    public boolean isRequiredAvailable() {
        return true;
    }

    public boolean isNonEmpty(String field) {
        return field != null && !field.trim().isEmpty();
    }
}
