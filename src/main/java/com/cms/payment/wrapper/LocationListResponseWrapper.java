package com.cms.payment.wrapper;

import com.cms.payment.domain.response.TuitionClassListResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationListResponseWrapper {
    private String message;
    private int statusCode;
    private TuitionClassListResponseDto data;
}
