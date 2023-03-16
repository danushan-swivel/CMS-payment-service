package com.cms.payment.domain.response;

import com.cms.payment.domain.entity.Payment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class PaymentReportListResponseDto extends ResponseDto{
    private final List<PaymentResponseDto> paidUsers;
    private final List<UnPaidUserResponseDto> unPaidUsers;

    public PaymentReportListResponseDto(Page<Payment> paymentPage, Map<String, StudentResponseDto> studentMap,
                                        Map<String, TuitionClassResponseDto> locationMap) {
        this.paidUsers = convertToPaidUser(paymentPage, studentMap, locationMap);
        this.unPaidUsers = convertToUnPaidUsers(paymentPage, studentMap, locationMap);
    }

    private List<PaymentResponseDto> convertToPaidUser(Page<Payment> paymentPage, Map<String,
            StudentResponseDto> studentMap, Map<String, TuitionClassResponseDto> tuitionClassMap) {
        return paymentPage.stream().map(payment -> {
            var studentResponse = studentMap.get(payment.getStudentId());
            var locationResponse = tuitionClassMap.get(studentResponse.getTuitionClassId());
            return new PaymentResponseDto(payment, studentResponse, locationResponse);
        }).collect(Collectors.toList());
    }

    private List<UnPaidUserResponseDto> convertToUnPaidUsers(Page<Payment> paymentPage, Map<String,
            StudentResponseDto> studentMap, Map<String, TuitionClassResponseDto> tuitionClassMap) {
            for (Payment payment : paymentPage.toList()) {
                studentMap.remove(payment.getStudentId());
            }
            List<StudentResponseDto> studentList = new ArrayList<>(studentMap.values());
            return studentList.stream().map(studentResponse -> {
                var locationResponse = tuitionClassMap.get(studentResponse.getTuitionClassId());
                return new UnPaidUserResponseDto(studentResponse, locationResponse);
            }).collect(Collectors.toList());
    }
}
