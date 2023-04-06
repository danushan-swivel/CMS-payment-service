package com.cms.payment.wrapper;

import com.cms.payment.domain.response.ResponseDto;
import com.cms.payment.enums.SuccessResponseStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class SuccessResponseWrapper extends ResponseWrapper{
    public SuccessResponseWrapper(SuccessResponseStatus responseStatus, ResponseDto data, HttpStatus httpStatus) {
        super(responseStatus.getMessage(), httpStatus.value(), data);
    }
}
