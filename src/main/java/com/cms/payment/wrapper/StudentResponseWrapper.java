package com.cms.payment.wrapper;

import com.cms.payment.domain.response.ResponseDto;
import com.cms.payment.domain.response.StudentResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentResponseWrapper {
    private String message;
    private int statusCode;
    private StudentResponseDto data;
}
