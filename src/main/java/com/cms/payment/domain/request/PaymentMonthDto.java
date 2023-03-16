package com.cms.payment.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentMonthDto {
    private String month;
    private int year;

    public String getCombinedDate() {
        return this.month + " " + this.year;
    }
}
