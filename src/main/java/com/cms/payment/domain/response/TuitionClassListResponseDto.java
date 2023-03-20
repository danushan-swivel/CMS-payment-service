package com.cms.payment.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TuitionClassListResponseDto {
    private List<TuitionClassResponseDto> locations;
}
