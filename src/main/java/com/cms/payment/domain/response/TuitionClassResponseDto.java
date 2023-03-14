package com.cms.payment.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class TuitionClassResponseDto {
    private String tuitionClassId;
    private String locationName;
    private String address;
    private String district;
    private String province;
    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;
}
