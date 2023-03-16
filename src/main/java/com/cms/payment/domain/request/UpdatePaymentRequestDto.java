package com.cms.payment.domain.request;

import lombok.Getter;

@Getter
public class UpdatePaymentRequestDto extends RequestDto {
    private String paymentId;
    private String studentId;
    private PaymentMonthDto paymentMonth;


    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(paymentMonth.getMonth()) && isNonEmpty(String.valueOf(paymentMonth.getYear()))
                && isNonEmpty(paymentId) && isNonEmpty(studentId);
    }
}
