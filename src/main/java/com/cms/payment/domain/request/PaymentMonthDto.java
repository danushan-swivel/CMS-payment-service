package com.cms.payment.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PaymentMonthDto extends RequestDto {
    private String month;
    private int year;

    public String getCombinedDate() {
        return this.month + " " + this.year;
    }
}
