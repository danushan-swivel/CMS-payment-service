package com.cms.payment.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class StudentListResponseDto {
    private List<StudentResponseDto> students;
}
