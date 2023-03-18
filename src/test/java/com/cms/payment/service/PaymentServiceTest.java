package com.cms.payment.service;

import com.cms.payment.domain.entity.Payment;
import com.cms.payment.domain.request.PaymentMonthDto;
import com.cms.payment.domain.request.PaymentRequestDto;
import com.cms.payment.domain.request.UpdatePaymentRequestDto;
import com.cms.payment.domain.response.StudentResponseDto;
import com.cms.payment.enums.ErrorResponseStatus;
import com.cms.payment.enums.SuccessResponseStatus;
import com.cms.payment.exception.*;
import com.cms.payment.repository.PaymentRepository;
import com.cms.payment.wrapper.StudentResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class PaymentServiceTest {

    private static final String STUDENT_BASE_URL = "http://localhost:8104/";
    private static final String LOCATION_BASE_URL = "http://localhost:8105/";
    private static final String GET_STUDENT_BY_ID_URL = "api/v1/student/##STUDENT-ID##";
    private static final String GET_ALL_LOCATION_URL = "api/v1/tuition/";
    private static final String GET_ALL_STUDENT_URL = "api/v1/student";
    private static final String PAYMENT_ID = "pid-1248=2598-7569-7458";
    private static final String PAYMENT_MONTH = "March 2023";
    private static final String UPDATED_PAYMENT_MONTH = "April 2023";
    private static final Date PAID_DATE = Date.valueOf("2023-03-15");
    private static final String STUDENT_ID = "sid-1254-7854-6485";
    private static final String ACCESS_TOKEN = "ey1365651-14156-51";
    private static final String FIRST_NAME = "Danushan";
    private static final String UPDATED_FIRST_NAME = "Danu";
    private static final String LAST_NAME = "Kanagasingam";
    private static final String UPDATED_LAST_NAME = "Danushan";
    private static final String ADDRESS = "A9 road, Vavuniya";
    private static final String GENDER = "Male";
    private static final String TUITION_CLASS_ID = "tid-1254-9654-7854-8955";
    private static final int PHONE_NUMBER = 771109101;
    private static final int AGE = 27;
    private static final int GRADE = 12;
    private static final int PAGE = 0;
    private static final int SIZE = 100;
    private static final String DEFAULT_SORT = "updated_at";

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RestTemplate restTemplate;
    private PaymentService paymentService;


    @BeforeEach
    void setUp() {
        openMocks(this);
        paymentService = new PaymentService(paymentRepository, restTemplate, STUDENT_BASE_URL, LOCATION_BASE_URL,
                GET_STUDENT_BY_ID_URL, GET_ALL_LOCATION_URL, GET_ALL_STUDENT_URL);
    }

    @Test
    void Should_ReturnPayment_When_MadePaymentSuccessfully() {
        PaymentRequestDto paymentRequestDto = getSamplePaymentRequestDto();
        Payment payment = getSamplePayment();
        StudentResponseWrapper studentResponseWrapper = getSampleStudentResponseWrapper();
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenReturn(ResponseEntity.of(Optional.of(studentResponseWrapper)));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        assertEquals(payment, paymentService.makePayment(paymentRequestDto, ACCESS_TOKEN));
    }

    @Test
    void Should_ThrowPaymentAlreadyExistsException_When_AlreadyPaymentIsMade() {
        PaymentRequestDto paymentRequestDto = getSamplePaymentRequestDto();
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID)).thenReturn(true);
        PaymentAlreadyExistsException exception = assertThrows(PaymentAlreadyExistsException.class, () ->
                paymentService.makePayment(paymentRequestDto, ACCESS_TOKEN));
        assertEquals("The payment already made for : " + PAYMENT_MONTH, exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentException_When_CheckingPaymentExistsIsFailed() {
        PaymentRequestDto paymentRequestDto = getSamplePaymentRequestDto();
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.makePayment(paymentRequestDto, ACCESS_TOKEN));
        assertEquals("Checking the existing payment is failed", exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidStudentException_When_InvalidStudentIdIsProvided() {
        PaymentRequestDto paymentRequestDto = getSamplePaymentRequestDto();
        StudentResponseWrapper studentResponseWrapper = getSampleStudentResponseWrapper();
        studentResponseWrapper.setStatusCode(ErrorResponseStatus.INTERNAL_SERVER_ERROR.getStatusCode());
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenReturn(ResponseEntity.of(Optional.of(studentResponseWrapper)));
        InvalidStudentException exception = assertThrows(InvalidStudentException.class, () ->
                paymentService.makePayment(paymentRequestDto, ACCESS_TOKEN));
        assertEquals("Invalid student Id : " + STUDENT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowConnectionException_When_StudentServiceNotAvailable() {
        PaymentRequestDto paymentRequestDto = getSamplePaymentRequestDto();
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenThrow(new ResourceAccessException("ERROR"));
        ConnectionException exception = assertThrows(ConnectionException.class, () ->
                paymentService.makePayment(paymentRequestDto, ACCESS_TOKEN));
        assertEquals("The requested resource couldn't access due to unavailability", exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentException_When_CheckStudentInStudentServiceIsFailed() {
        PaymentRequestDto paymentRequestDto = getSamplePaymentRequestDto();
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.makePayment(paymentRequestDto, ACCESS_TOKEN));
        assertEquals("Validating student identity is failed", exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentException_When_SavingPaymentIsFailed() {
        PaymentRequestDto paymentRequestDto = getSamplePaymentRequestDto();
        Payment payment = getSamplePayment();
        StudentResponseWrapper studentResponseWrapper = getSampleStudentResponseWrapper();
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenReturn(ResponseEntity.of(Optional.of(studentResponseWrapper)));
        when(paymentRepository.save(any(Payment.class))).thenThrow(new DataAccessException("ERROR") {
        });
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.makePayment(paymentRequestDto, ACCESS_TOKEN));
        assertEquals("Saving payment details into database is failed.", exception.getMessage());
    }

//    update payment

    @Test
    void Should_ReturnPayment_When_PaymentUpdatedSuccessfully() {
        UpdatePaymentRequestDto updatePaymentRequestDto = getSampleUpdatePaymentRequestDto();
        StudentResponseWrapper studentResponseWrapper = getSampleStudentResponseWrapper();
        Payment payment = getSamplePayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndPaymentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID, PAYMENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenReturn(ResponseEntity.of(Optional.of(studentResponseWrapper)));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        assertEquals(payment, paymentService.updatePayment(updatePaymentRequestDto, ACCESS_TOKEN));
    }

    @Test
    void Should_ThrowPaymentAlreadyExistsException_When_InvalidPaymentIdIsProvidedForUpdatePayment() {
        UpdatePaymentRequestDto updatePaymentRequestDto = getSampleUpdatePaymentRequestDto();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());
        InvalidPaymentException exception = assertThrows(InvalidPaymentException.class, () ->
                paymentService.updatePayment(updatePaymentRequestDto, ACCESS_TOKEN));
        assertEquals("Invalid payment Id : " + PAYMENT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentException_When_GettingPaymentFromDatabaseIsFailed() {
        UpdatePaymentRequestDto updatePaymentRequestDto = getSampleUpdatePaymentRequestDto();
        when(paymentRepository.findById(PAYMENT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.updatePayment(updatePaymentRequestDto, ACCESS_TOKEN));
        assertEquals("Retrieving payment from database is failed for " + PAYMENT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentAlreadyExistsException_When_AlreadyPaymentIsMadeForUpdatePayment() {
        UpdatePaymentRequestDto updatePaymentRequestDto = getSampleUpdatePaymentRequestDto();
        Payment payment = getSamplePayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndPaymentIdAndIsDeletedFalse(UPDATED_PAYMENT_MONTH, STUDENT_ID, PAYMENT_ID)).thenReturn(true);
        PaymentAlreadyExistsException exception = assertThrows(PaymentAlreadyExistsException.class, () ->
                paymentService.updatePayment(updatePaymentRequestDto, ACCESS_TOKEN));
        assertEquals("The payment already made for : " + UPDATED_PAYMENT_MONTH, exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentException_When_CheckingPaymentExistsIsFailedForUpdatePayment() {
        UpdatePaymentRequestDto updatePaymentRequestDto = getSampleUpdatePaymentRequestDto();
        Payment payment = getSamplePayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndPaymentIdAndIsDeletedFalse(UPDATED_PAYMENT_MONTH, STUDENT_ID, PAYMENT_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.updatePayment(updatePaymentRequestDto, ACCESS_TOKEN));
        assertEquals("Checking the existing payment is failed", exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidStudentException_When_InvalidStudentIdIsProvidedForUpdatePayment() {
        UpdatePaymentRequestDto updatePaymentRequestDto = getSampleUpdatePaymentRequestDto();
        StudentResponseWrapper studentResponseWrapper = getSampleStudentResponseWrapper();
        studentResponseWrapper.setStatusCode(ErrorResponseStatus.INTERNAL_SERVER_ERROR.getStatusCode());
        Payment payment = getSamplePayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndPaymentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID, PAYMENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenReturn(ResponseEntity.of(Optional.of(studentResponseWrapper)));
        InvalidStudentException exception = assertThrows(InvalidStudentException.class, () ->
                paymentService.updatePayment(updatePaymentRequestDto, ACCESS_TOKEN));
        assertEquals("Invalid student Id : " + STUDENT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowConnectionException_When_StudentServiceNotAvailableForUpdatePayment() {
        UpdatePaymentRequestDto updatePaymentRequestDto = getSampleUpdatePaymentRequestDto();
        Payment payment = getSamplePayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndPaymentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID, PAYMENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenThrow(new ResourceAccessException("ERROR"));
        ConnectionException exception = assertThrows(ConnectionException.class, () ->
                paymentService.updatePayment(updatePaymentRequestDto, ACCESS_TOKEN));
        assertEquals("The requested resource couldn't access due to unavailability", exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentException_When_CheckStudentInStudentServiceIsFailedForUpdatePayment() {
        UpdatePaymentRequestDto updatePaymentRequestDto = getSampleUpdatePaymentRequestDto();
        Payment payment = getSamplePayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndPaymentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID, PAYMENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.updatePayment(updatePaymentRequestDto, ACCESS_TOKEN));
        assertEquals("Validating student identity is failed", exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentException_When_SavingPaymentIsFailedForUpdatePayment() {
        UpdatePaymentRequestDto updatePaymentRequestDto = getSampleUpdatePaymentRequestDto();
        Payment payment = getSamplePayment();
        StudentResponseWrapper studentResponseWrapper = getSampleStudentResponseWrapper();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.existsByPaymentMonthAndStudentIdAndPaymentIdAndIsDeletedFalse(PAYMENT_MONTH, STUDENT_ID, PAYMENT_ID)).thenReturn(false);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentResponseWrapper.class))).thenReturn(ResponseEntity.of(Optional.of(studentResponseWrapper)));
        when(paymentRepository.save(any(Payment.class))).thenThrow(new DataAccessException("ERROR") {
        });
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.updatePayment(updatePaymentRequestDto, ACCESS_TOKEN));
        assertEquals("Updating payment is failed for " + PAYMENT_ID, exception.getMessage());
    }

    @Test
    void Should_DeletePayment_When_ValidPaymentIdIsProvided() {
        Payment payment = getSamplePayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        paymentService.deletePayment(PAYMENT_ID);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void Should_ThrowPaymentException_When_DeletePaymentIsFailed() {
        Payment payment = getSamplePayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenThrow(new DataAccessException("ERROR") {
        });
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.deletePayment(PAYMENT_ID));
        assertEquals("Deleting payment from database is failed for " + PAYMENT_ID, exception.getMessage());
    }

    @Test
    void Should_ReturnPaymentPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        Page<Payment> paymentPage = getSamplePaymentPage();
        when(paymentRepository.findAll(pageable)).thenReturn(paymentPage);
        assertEquals(paymentPage, paymentService.getAllPayment());
    }

    @Test
    void Should_ThrowPaymentException_When_GetAllPaymentsIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(paymentRepository.findAll(pageable)).thenThrow(new DataAccessException("ERROR") {
        });
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.getAllPayment());
        assertEquals("Retrieving Payment list from database is failed.", exception.getMessage());
    }


    @AfterEach
    void tearDown() {
    }

    private Payment getSamplePayment() {
        Payment payment = new Payment();
        payment.setPaymentId(PAYMENT_ID);
        payment.setPaymentMonth(PAYMENT_MONTH);
        payment.setPaidDate(PAID_DATE);
        payment.setStudentId(STUDENT_ID);
        payment.setDeleted(false);
        return payment;
    }

    private PaymentRequestDto getSamplePaymentRequestDto() {
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto();
        PaymentMonthDto paymentMonthDto = new PaymentMonthDto("March", 2023);
        paymentRequestDto.setStudentId(STUDENT_ID);
        paymentRequestDto.setPaymentMonth(paymentMonthDto);
        return paymentRequestDto;
    }

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

    private StudentResponseWrapper getSampleStudentResponseWrapper() {
        StudentResponseDto studentResponseDto = getSampleStudentRequestDto();
        StudentResponseWrapper studentResponseWrapper = new StudentResponseWrapper();
        studentResponseWrapper.setMessage(SuccessResponseStatus.READ_STUDENT.getMessage());
        studentResponseWrapper.setStatusCode(SuccessResponseStatus.READ_STUDENT.getStatusCode());
        studentResponseWrapper.setData(studentResponseDto);
        return studentResponseWrapper;
    }

    private Page<Payment> getSamplePaymentPage() {
        List<Payment> payments = new ArrayList<>();
        Payment payment = getSamplePayment();
        payments.add(payment);
        return new PageImpl<>(payments);
    }
}