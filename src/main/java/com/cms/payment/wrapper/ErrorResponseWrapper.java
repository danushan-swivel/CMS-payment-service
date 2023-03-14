package com.cms.payment.wrapper;

import com.cms.payment.domain.response.ResponseDto;
import com.cms.payment.enums.ErrorResponseStatus;
import com.cms.payment.enums.SuccessResponseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponseWrapper extends ResponseWrapper{
    public ErrorResponseWrapper(ErrorResponseStatus responseStatus, ResponseDto data) {
        super(responseStatus.getMessage(), responseStatus.getStatusCode(), data);
    }
}
