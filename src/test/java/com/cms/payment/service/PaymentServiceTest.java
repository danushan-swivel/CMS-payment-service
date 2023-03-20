package com.cms.payment.service;

import com.cms.payment.domain.entity.Payment;
import com.cms.payment.domain.request.PaymentMonthDto;
import com.cms.payment.domain.request.PaymentRequestDto;
import com.cms.payment.domain.request.UpdatePaymentRequestDto;
import com.cms.payment.domain.response.StudentListResponseDto;
import com.cms.payment.domain.response.StudentResponseDto;
import com.cms.payment.domain.response.TuitionClassListResponseDto;
import com.cms.payment.domain.response.TuitionClassResponseDto;
import com.cms.payment.enums.ErrorResponseStatus;
import com.cms.payment.enums.SuccessResponseStatus;
import com.cms.payment.exception.*;
import com.cms.payment.repository.PaymentRepository;
import com.cms.payment.wrapper.StudentListResponseWrapper;
import com.cms.payment.wrapper.StudentResponseWrapper;
import com.cms.payment.wrapper.TuitionClassListResponseWrapper;
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
import java.util.*;

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

    @AfterEach
    void tearDown() {
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

    @Test
    void Should_ReturnPaymentPage_When_StudentIdIsProvided() {
        Page<Payment> paymentPage = getSamplePaymentPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(paymentRepository.findByStudentId(pageable, STUDENT_ID)).thenReturn(paymentPage);
        assertEquals(paymentPage, paymentService.getPaymentsByStudentId(STUDENT_ID));
    }

    @Test
    void Should_ThrowPaymentException_When_GetPaymentDetailsFromDatabaseIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(paymentRepository.findByStudentId(pageable, STUDENT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.getPaymentsByStudentId(STUDENT_ID));
        assertEquals("Retrieving Payment list for student id: " + STUDENT_ID
                + " from database is failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnStudentMap_When_StudentDetailsReceivedSuccessfully() {
        StudentListResponseWrapper studentListResponseWrapper = getSampleStudentListResponseWrapper();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentListResponseWrapper.class))).thenReturn(ResponseEntity.of(Optional.of(studentListResponseWrapper)));
        var result = paymentService.getStudentsDetails(ACCESS_TOKEN);
        assertEquals(STUDENT_ID, result.get(STUDENT_ID).getStudentId());
        assertEquals(FIRST_NAME, result.get(STUDENT_ID).getFirstName());
        assertEquals(LAST_NAME, result.get(STUDENT_ID).getLastName());
    }

    @Test
    void Should_ThrowConnectionException_When_StudentServiceNotAvailableForGetStudentDetails() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentListResponseWrapper.class))).thenThrow(new ResourceAccessException("ERROR"));
        ConnectionException exception = assertThrows(ConnectionException.class, () ->
                paymentService.getStudentsDetails(ACCESS_TOKEN));
        assertEquals("The requested resource couldn't access due to unavailability", exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentException_When_StudentServiceNotAvailable() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(StudentListResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.getStudentsDetails(ACCESS_TOKEN));
        assertEquals("The requesting data is failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnTuitionClassMap_When_TuitionClassDetailsReceivedSuccessfully() {
        TuitionClassListResponseWrapper tuitionClassListResponseWrapper = getSampleTuitionClassListResponseWrapper();
        Map<String, TuitionClassResponseDto> tuitionMap = getSampleTuitionClassMaps();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(TuitionClassListResponseWrapper.class))).thenReturn(ResponseEntity.of(Optional.of(tuitionClassListResponseWrapper)));
        var result = paymentService.getTuitionClassDetails(ACCESS_TOKEN);
        assertEquals(TUITION_CLASS_ID, result.get(TUITION_CLASS_ID).getTuitionClassId());
        assertEquals(DISTRICT, result.get(TUITION_CLASS_ID).getDistrict());
        assertEquals(PROVINCE, result.get(TUITION_CLASS_ID).getProvince());
    }

    @Test
    void Should_ThrowConnectionException_When_TuitionClassServiceNotAvailableForGetTuitionClassDetails() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(TuitionClassListResponseWrapper.class))).thenThrow(new ResourceAccessException("ERROR"));
        ConnectionException exception = assertThrows(ConnectionException.class, () ->
                paymentService.getTuitionClassDetails(ACCESS_TOKEN));
        assertEquals("The requested resource couldn't access due to unavailability", exception.getMessage());
    }

    @Test
    void Should_ThrowPaymentException_When_TuitionClassServiceNotAvailable() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(TuitionClassListResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.getTuitionClassDetails(ACCESS_TOKEN));
        assertEquals("The requesting data is failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnPaymentPage_When_WhenMonthAndYearIsProvided() {
        Page<Payment> paymentPage = getSamplePaymentPage();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(paymentRepository.findByPaymentMonth(pageable, PAYMENT_MONTH)).thenReturn(paymentPage);
        assertEquals(paymentPage, paymentService.getUserReport("March", 2023));
    }

    @Test
    void Should_ThrowPaymentException_When_GetStudentReportIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(paymentRepository.findByPaymentMonth(pageable, PAYMENT_MONTH)).thenThrow(new DataAccessException("ERROR") { });
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.getUserReport("March", 2023));
        assertEquals("Retrieving the payment reports from database is failed", exception.getMessage());
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
     * This method creates sample student response wrapper
     *
     * @return StudentResponseWrapper
     */
    private StudentResponseWrapper getSampleStudentResponseWrapper() {
        StudentResponseDto studentResponseDto = getSampleStudentRequestDto();
        StudentResponseWrapper studentResponseWrapper = new StudentResponseWrapper();
        studentResponseWrapper.setMessage(SuccessResponseStatus.READ_STUDENT.getMessage());
        studentResponseWrapper.setStatusCode(SuccessResponseStatus.READ_STUDENT.getStatusCode());
        studentResponseWrapper.setData(studentResponseDto);
        return studentResponseWrapper;
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

    /**
     * This method creates sample student list response dto
     *
     * @return StudentListResponseDto
     */
    private StudentListResponseDto getSampleStudentListResponseDto() {
        StudentResponseDto studentResponseDto = getSampleStudentRequestDto();
        List<StudentResponseDto> studentResponseDtos = new ArrayList<>();
        studentResponseDtos.add(studentResponseDto);
        StudentListResponseDto studentListResponseDto = new StudentListResponseDto();
        studentListResponseDto.setStudents(studentResponseDtos);
        return studentListResponseDto;
    }

    /**
     * This method creates sample student list response wrapper
     *
     * @return StudentListResponseWrapper
     */
    private StudentListResponseWrapper getSampleStudentListResponseWrapper() {
        StudentListResponseDto studentListResponseDto = getSampleStudentListResponseDto();
        StudentListResponseWrapper studentListResponseWrapper = new StudentListResponseWrapper();
        studentListResponseWrapper.setData(studentListResponseDto);
        studentListResponseWrapper.setMessage(SuccessResponseStatus.READ_STUDENT_LIST.getMessage());
        studentListResponseWrapper.setStatusCode(SuccessResponseStatus.READ_STUDENT_LIST.getStatusCode());
        return studentListResponseWrapper;
    }

    /**
     * This method creates sample tuition class list response dto
     *
     * @return TuitionClassListResponseDto
     */
    private TuitionClassListResponseDto getSampleTuitionClassListResponseDto() {
        TuitionClassResponseDto tuitionClassResponseDto = getSampleTuitionClassResponseDto();
        List<TuitionClassResponseDto> tuitionClassResponseDtoList = new ArrayList<>();
        tuitionClassResponseDtoList.add(tuitionClassResponseDto);
        TuitionClassListResponseDto tuitionClassListResponseDto = new TuitionClassListResponseDto();
        tuitionClassListResponseDto.setLocations(tuitionClassResponseDtoList);
        return tuitionClassListResponseDto;
    }

    /**
     * This method creates sample tuition class list response wrapper
     *
     * @return TuitionClassListResponseWrapper
     */
    private TuitionClassListResponseWrapper getSampleTuitionClassListResponseWrapper() {
        TuitionClassListResponseDto tuitionClassListResponseDto = getSampleTuitionClassListResponseDto();
        TuitionClassListResponseWrapper tuitionClassListResponseWrapper = new TuitionClassListResponseWrapper();
        tuitionClassListResponseWrapper.setMessage(SuccessResponseStatus.READ_LOCATION_LIST.getMessage());
        tuitionClassListResponseWrapper.setStatusCode(SuccessResponseStatus.READ_LOCATION_LIST.getStatusCode());
        tuitionClassListResponseWrapper.setData(tuitionClassListResponseDto);
        return tuitionClassListResponseWrapper;
    }
}