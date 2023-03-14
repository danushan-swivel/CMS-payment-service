package com.cms.payment.wrapper;

import com.cms.payment.domain.response.ResponseDto;
import com.cms.payment.enums.SuccessResponseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuccessResponseWrapper extends ResponseWrapper{
    public SuccessResponseWrapper(SuccessResponseStatus responseStatus, ResponseDto data) {
        super(responseStatus.getMessage(), responseStatus.getStatusCode(), data);
    }
}
