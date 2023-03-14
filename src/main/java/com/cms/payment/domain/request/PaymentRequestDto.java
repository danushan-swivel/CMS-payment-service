package com.cms.payment.domain.request;

import lombok.Getter;

@Getter
public class PaymentRequestDto extends RequestDto{
    private PaymentMonthDto paymentMonth;
    private String studentId;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(paymentMonth.getMonth()) && isNonEmpty(String.valueOf(paymentMonth.getYear()))
                && isNonEmpty(studentId);
    }
}
