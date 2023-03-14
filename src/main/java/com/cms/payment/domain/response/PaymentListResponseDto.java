package com.cms.payment.domain.response;

import com.cms.payment.domain.entity.Payment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class PaymentListResponseDto extends ResponseDto{
    private final List<PaymentResponseDto> payments;

    public PaymentListResponseDto(Page<Payment> paymentPage, Map<String, StudentResponseDto> studentMap,
                                  Map<String, TuitionClassResponseDto> locationMap) {
        this.payments = convertToResponseDto(paymentPage, studentMap, locationMap);
    }

    private List<PaymentResponseDto> convertToResponseDto(Page<Payment> paymentPage, Map<String,
            StudentResponseDto> studentMap, Map<String, TuitionClassResponseDto> tuitionClassMap) {
        return paymentPage.stream().map(payment -> {
            var studentResponse = studentMap.get(payment.getStudentId());
            var locationResponse = tuitionClassMap.get(studentResponse.getTuitionClassId());
            return paymentResponseDto = new PaymentResponseDto(payment, studentResponse, locationResponse);
        }).collect(Collectors.toList());
    }
}
