package com.cms.payment.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class StudentResponseDto {
    private String studentId;
    private String firstName;
    private String lastName;
    private String address;
    private String gender;
    private int age;
    private int grade;
    private int phoneNumber;
    private String studentStatus;
    private String tuitionClassId;
    private Date joinedDate;
    private Date updatedAt;
    private boolean isDeleted;
}
