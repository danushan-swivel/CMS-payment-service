package com.cms.payment.domain.response;

import com.cms.payment.domain.entity.Payment;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class PaymentResponseDto extends ResponseDto{
    private String paymentId;
    private String paymentMonth;
    private Date paidDate;
    private StudentResponseDto studentDetails;
    private TuitionClassResponseDto locationDetails;
    private Date updatedAt;
    private boolean isDeleted;

    public PaymentResponseDto(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.paymentMonth = payment.getPaymentMonth();
        this.paidDate = payment.getPaidDate();
        this.studentDetails = null;
        this.updatedAt = payment.getUpdatedAt();
        this.isDeleted = payment.isDeleted();
    }

    public PaymentResponseDto(Payment payment, StudentResponseDto studentResponseDto,
                              TuitionClassResponseDto locationResponseDto) {
        this.paymentId = payment.getPaymentId();
        this.paymentMonth = payment.getPaymentMonth();
        this.paidDate = payment.getPaidDate();
        this.studentDetails = studentResponseDto;
        this.locationDetails = locationResponseDto;
        this.updatedAt = payment.getUpdatedAt();
        this.isDeleted = payment.isDeleted();
    }
}
