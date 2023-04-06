package com.cms.payment.domain.entity;

import com.cms.payment.domain.request.PaymentRequestDto;
import com.cms.payment.domain.request.UpdatePaymentRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment")
@Entity
public class Payment {
    private static final String PREFIX = "pid-";
    @Id
    @Column(length = 50)
    private String paymentId;
    @Column(length = 20)
    private String paymentMonth;
    private Date paidDate;
    @Column(length = 50)
    private String studentId;
    private Date updatedAt;
    private boolean isDeleted;

    public Payment(PaymentRequestDto paymentRequestDto) {
        this.paymentId = PREFIX + UUID.randomUUID();
        this.paymentMonth = paymentRequestDto.getPaymentMonth().getCombinedDate();
        this.studentId = paymentRequestDto.getStudentId();
        this.paidDate = this.updatedAt = new Date(System.currentTimeMillis());
        this.isDeleted = false;
    }

    public void update(UpdatePaymentRequestDto updatePaymentRequestDto) {
        this.paymentId = updatePaymentRequestDto.getPaymentId();
        this.paymentMonth = updatePaymentRequestDto.getPaymentMonth().getCombinedDate();
        this.updatedAt = new Date(System.currentTimeMillis());
    }
}
