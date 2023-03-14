package com.cms.payment.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PaymentMonthDto {
    private String month;
    private int year;

    public String getCombinedDate() {
        return this.month +" " + this.year;
    }
}
