package com.cms.payment.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnPaidUserResponseDto extends ResponseDto {
    private StudentResponseDto studentDetails;
    private TuitionClassResponseDto locationDetails;

    public UnPaidUserResponseDto(StudentResponseDto studentResponseDto,
                                 TuitionClassResponseDto locationResponseDto) {

        this.studentDetails = studentResponseDto;
        this.locationDetails = locationResponseDto;
    }
}
