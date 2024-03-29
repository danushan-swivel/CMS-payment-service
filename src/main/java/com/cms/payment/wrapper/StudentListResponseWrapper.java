package com.cms.payment.wrapper;

import com.cms.payment.domain.response.StudentListResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentListResponseWrapper {
    private String message;
    private int statusCode;
    private StudentListResponseDto data;
}
