package com.cms.payment.controller;

import com.cms.payment.domain.entity.Payment;
import com.cms.payment.domain.request.PaymentMonthDto;
import com.cms.payment.domain.request.PaymentRequestDto;
import com.cms.payment.domain.request.UpdatePaymentRequestDto;
import com.cms.payment.domain.response.StudentResponseDto;
import com.cms.payment.domain.response.TuitionClassResponseDto;
import com.cms.payment.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.MockitoAnnotations.openMocks;

class PaymentControllerTest {

    private static final String PAYMENT_BASE_URL= "api/v1/payment";

    private static final String PAYMENT_ID = "pid-1248=2598-7569-7458";
    private static final String PAYMENT_MONTH = "March 2023";
    private static final String UPDATED_PAYMENT_MONTH = "April 2023";
    private static final Date PAID_DATE = Date.valueOf("2023-03-15");
    private static final String STUDENT_ID = "sid-1254-7854-6485";
    private static final String ACCESS_TOKEN = "ey1365651-14156-51";
    private static final String FIRST_NAME = "Danushan";
    private static final String LAST_NAME = "Kanagasingam";
    private static final String ADDRESS = "A9 road, Vavuniya";
    private static final String GENDER = "Male";
    private static final String TUITION_CLASS_ID = "tid-1254-9654-7854-8955";
    private static final String LOCATION_NAME = "Elegance";
    private static final String DISTRICT = "Colombo";
    private static final String LOCATION_ADDRESS = "Queen's Road, Duplication Road";
    private static final String PROVINCE = "South";
    private static final int PHONE_NUMBER = 771109101;
    private static final int AGE = 27;
    private static final int GRADE = 12;

    @Mock
    private PaymentService paymentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        openMocks(this);
        PaymentController paymentController = new PaymentController(paymentService);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void Should_ReturnOk_When_MakePaymentSuccessful() throws Exception {
        PaymentRequestDto paymentRequestDto = getSamplePaymentRequestDto();

    }


    /**
     * This method return sample payment
     *
     * @return Payment
     */
    private Payment getSamplePayment() {
        Payment payment = new Payment();
        payment.setPaymentId(PAYMENT_ID);
        payment.setPaymentMonth(PAYMENT_MONTH);
        payment.setPaidDate(PAID_DATE);
        payment.setStudentId(STUDENT_ID);
        payment.setDeleted(false);
        return payment;
    }

    /**
     * This method return sample payment request dto
     *
     * @return PaymentRequestDto
     */
    private PaymentRequestDto getSamplePaymentRequestDto() {
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto();
        PaymentMonthDto paymentMonthDto = new PaymentMonthDto("March", 2023);
        paymentRequestDto.setStudentId(STUDENT_ID);
        paymentRequestDto.setPaymentMonth(paymentMonthDto);
        return paymentRequestDto;
    }

    /**
     * This method creates sample update payment request dto
     *
     * @return UpdatePaymentRequestDto
     */
    private UpdatePaymentRequestDto getSampleUpdatePaymentRequestDto() {
        UpdatePaymentRequestDto updatePaymentRequestDto = new UpdatePaymentRequestDto();
        PaymentMonthDto paymentMonthDto = new PaymentMonthDto("April", 2023);
        updatePaymentRequestDto.setStudentId(STUDENT_ID);
        updatePaymentRequestDto.setPaymentMonth(paymentMonthDto);
        updatePaymentRequestDto.setPaymentId(PAYMENT_ID);
        return updatePaymentRequestDto;
    }

    /**
     * This method creates sample student request dto
     *
     * @return StudentRequestDto
     */
    private StudentResponseDto getSampleStudentRequestDto() {
        StudentResponseDto studentResponseDto = new StudentResponseDto();
        studentResponseDto.setStudentId(STUDENT_ID);
        studentResponseDto.setAddress(ADDRESS);
        studentResponseDto.setAge(AGE);
        studentResponseDto.setGrade(GRADE);
        studentResponseDto.setPhoneNumber(PHONE_NUMBER);
        studentResponseDto.setGender(GENDER);
        studentResponseDto.setFirstName(FIRST_NAME);
        studentResponseDto.setLastName(LAST_NAME);
        studentResponseDto.setTuitionClassId(TUITION_CLASS_ID);
        return studentResponseDto;
    }

    /**
     * This method creates sample payment page
     *
     * @return PaymentPage
     */
    private Page<Payment> getSamplePaymentPage() {
        List<Payment> payments = new ArrayList<>();
        Payment payment = getSamplePayment();
        payments.add(payment);
        return new PageImpl<>(payments);
    }

    /**
     * This method creates sample student details map
     *
     * @return StudentMap
     */
    private Map<String, StudentResponseDto> getSampleStudentsMaps() {
        StudentResponseDto studentResponseDto = getSampleStudentRequestDto();
        Map<String, StudentResponseDto> studentsMap = new HashMap<>();
        studentsMap.put(studentResponseDto.getStudentId(), studentResponseDto);
        return studentsMap;
    }

    /**
     * This method creates sample tuition class response dto
     *
     * @return TuitionClassResponseDto
     */
    private TuitionClassResponseDto getSampleTuitionClassResponseDto() {
        TuitionClassResponseDto tuitionClassResponseDto = new TuitionClassResponseDto();
        tuitionClassResponseDto.setTuitionClassId(TUITION_CLASS_ID);
        tuitionClassResponseDto.setProvince(PROVINCE);
        tuitionClassResponseDto.setLocationName(LOCATION_NAME);
        tuitionClassResponseDto.setAddress(LOCATION_ADDRESS);
        tuitionClassResponseDto.setDistrict(DISTRICT);
        return tuitionClassResponseDto;
    }

    /**
     * This method creates sample tuition class details map
     *
     * @return TuitionMap
     */
    private Map<String, TuitionClassResponseDto> getSampleTuitionClassMaps() {
        TuitionClassResponseDto tuitionClassResponseDto = getSampleTuitionClassResponseDto();
        Map<String, TuitionClassResponseDto> tuitionMap = new HashMap<>();
        tuitionMap.put(tuitionClassResponseDto.getTuitionClassId(), tuitionClassResponseDto);
        return tuitionMap;
    }
}