package com.cms.payment.service;

import com.cms.payment.domain.entity.Payment;
import com.cms.payment.domain.request.PaymentMonthDto;
import com.cms.payment.domain.request.PaymentRequestDto;
import com.cms.payment.domain.request.UpdatePaymentRequestDto;
import com.cms.payment.domain.response.StudentResponseDto;
import com.cms.payment.domain.response.TuitionClassResponseDto;
import com.cms.payment.enums.SuccessResponseStatus;
import com.cms.payment.exception.*;
import com.cms.payment.repository.PaymentRepository;
import com.cms.payment.utills.Constants;
import com.cms.payment.wrapper.StudentListResponseWrapper;
import com.cms.payment.wrapper.StudentResponseWrapper;
import com.cms.payment.wrapper.TuitionClassListResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Payment Service
 */
@Service
public class PaymentService {

    private static final int PAGE = 0;
    private static final int SIZE = 100;
    private static final String DEFAULT_SORT = "updated_at";
    private static final String INVALID_PAYMENT_ID_MESSAGE = "Invalid payment Id : ";
    private static final String INVALID_STUDENT_ID_MESSAGE = "Invalid student Id : ";
    private static final String CONNECTION_EXCEPTION_MESSAGE = "The requested resource couldn't access due to unavailability";
    private static final String STUDENT_ID_REPLACE_PHRASE = "##STUDENT-ID##";
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final String getStudentByIdUrl;
    private final String getAllStudentDetails;
    private final String getAllLocationDetails;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, RestTemplate restTemplate,
                          @Value("${student.uri.baseUrl}") String studentBaseUrl,
                          @Value("${location.uri.baseUrl}") String locationBaseUrl,
                          @Value("${student.uri.getStudentById}") String getStudentById,
                          @Value("${location.uri.getAllLocationDetails}") String getAllLocationDetails,
                          @Value("${student.uri.getAllStudentDetails}") String getAllStudentDetails) {
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
        this.getStudentByIdUrl = studentBaseUrl + getStudentById;
        this.getAllStudentDetails = studentBaseUrl + getAllStudentDetails;
        this.getAllLocationDetails = locationBaseUrl + getAllLocationDetails;
    }

    /**
     * Method to create new payment
     *
     * @param paymentRequestDto payment request dto
     * @param authToken         access token
     * @return Payment
     */
    public Payment makePayment(PaymentRequestDto paymentRequestDto, String authToken) {
        try {
            Payment payment = new Payment(paymentRequestDto);
            if (checkExistsPayment(paymentRequestDto.getPaymentMonth(), paymentRequestDto.getStudentId(), null)) {
                throw new PaymentAlreadyExistsException("The payment already made for : "
                        + paymentRequestDto.getPaymentMonth().getCombinedDate());
            }
            String uri = getStudentByIdUrl.replace(STUDENT_ID_REPLACE_PHRASE, paymentRequestDto.getStudentId());
            if (!existsStudentId(uri, authToken)) {
                throw new InvalidStudentException(INVALID_STUDENT_ID_MESSAGE + paymentRequestDto.getStudentId());
            }
            return paymentRepository.save(payment);
        } catch (ResourceAccessException e) {
            throw new ConnectionException(CONNECTION_EXCEPTION_MESSAGE);
        } catch (HttpClientErrorException e) {
            throw new PaymentException("Validating student identity is failed", e);
        } catch (DataAccessException e) {
            throw new PaymentException("Saving payment details into database is failed.", e);
        }
    }

    /**
     * Get payment by payment id
     *
     * @param paymentId payment id
     * @return Payment
     */
    public Payment getPaymentById(String paymentId) {
        try {
            Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
            if (optionalPayment.isEmpty()) {
                throw new InvalidPaymentException(INVALID_PAYMENT_ID_MESSAGE + paymentId);
            }
            return optionalPayment.get();
        } catch (DataAccessException e) {
            throw new PaymentException("Retrieving payment from database is failed for " + paymentId);
        }
    }

    /**
     * Update the existing payment
     *
     * @param updatePaymentRequestDto update payment request dto
     * @param authToken               access token
     * @return Payment
     */
    public Payment updatePayment(UpdatePaymentRequestDto updatePaymentRequestDto, String authToken) {
        try {
            Payment paymentFromDB = getPaymentById(updatePaymentRequestDto.getPaymentId());
            if (checkExistsPayment(updatePaymentRequestDto.getPaymentMonth(), updatePaymentRequestDto.getStudentId(),
                    updatePaymentRequestDto.getPaymentId())) {
                throw new PaymentAlreadyExistsException("The payment already made for : "
                        + updatePaymentRequestDto.getPaymentMonth().getCombinedDate());
            }
            String uri = getStudentByIdUrl.replace(STUDENT_ID_REPLACE_PHRASE, paymentFromDB.getStudentId());
            if (!existsStudentId(uri, authToken)) {
                throw new InvalidStudentException(INVALID_STUDENT_ID_MESSAGE + updatePaymentRequestDto.getStudentId());
            }
            paymentFromDB.update(updatePaymentRequestDto);
            paymentRepository.save(paymentFromDB);
            return paymentFromDB;
        } catch (ResourceAccessException e) {
            throw new ConnectionException(CONNECTION_EXCEPTION_MESSAGE);
        } catch (HttpClientErrorException e) {
            throw new PaymentException("Validating student identity is failed", e);
        } catch (DataAccessException e) {
            throw new PaymentException("Updating payment is failed for " + updatePaymentRequestDto.getPaymentId());
        }
    }

    /**
     * Delete payment by payment id
     *
     * @param paymentId payment id
     */
    public void deletePayment(String paymentId) {
        try {
            Payment paymentFromDB = getPaymentById(paymentId);
            paymentFromDB.setDeleted(true);
            paymentFromDB.setUpdatedAt(new Date(System.currentTimeMillis()));
            paymentRepository.save(paymentFromDB);
        } catch (DataAccessException e) {
            throw new PaymentException("Deleting payment from database is failed for " + paymentId);
        }
    }

    /**
     * Get all payment details page
     *
     * @return PaymentPage
     */
    public Page<Payment> getAllPayment() {
        try {
            Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
            return paymentRepository.findAll(pageable);
        } catch (DataAccessException e) {
            throw new PaymentException("Retrieving Payment list from database is failed.");
        }
    }

    /**
     * Get payment details page for a student id
     *
     * @param studentId student id
     * @return PaymentPage
     */
    public Page<Payment> getPaymentsByStudentId(String studentId) {
        try {
            Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
            return paymentRepository.findByStudentId(pageable, studentId);
        } catch (DataAccessException e) {
            throw new PaymentException("Retrieving Payment list for student id: " + studentId
                    + " from database is failed.");
        }
    }

    /**
     * Get the student details with mapped with student id
     *
     * @param authToken access token
     * @return StudentResponseDtoMap
     */
    public Map<String, StudentResponseDto> getStudentsDetails(String authToken) {
        try {
            var headers = new HttpHeaders();
            headers.set("access_token", authToken);
            var entity = new HttpEntity<String>(headers);
            var studentResponse = restTemplate.exchange(getAllStudentDetails, HttpMethod.GET, entity,
                    StudentListResponseWrapper.class);
            var studentResponseList = Objects.requireNonNull(studentResponse.getBody()).getData().getStudents();
            Map<String, StudentResponseDto> studentDetailsMap = new HashMap<>();
            for (StudentResponseDto responseDto : studentResponseList) {
                studentDetailsMap.put(responseDto.getStudentId(), responseDto);
            }
            return studentDetailsMap;
        } catch (ResourceAccessException e) {
            throw new ConnectionException(CONNECTION_EXCEPTION_MESSAGE);
        } catch (HttpClientErrorException e) {
            throw new PaymentException("The requesting data is failed.", e);
        }
    }

    /**
     * Get tuition class map with tuition class id
     *
     * @param authToken access token
     * @return TuitionClasssResponseDtoMap
     */
    public Map<String, TuitionClassResponseDto> getTuitionClassDetails(String authToken) {
        try {
            var headers = new HttpHeaders();
            headers.set("access_token", authToken);
            var entity = new HttpEntity<String>(headers);
            var tuitionClassResponse = restTemplate.exchange(getAllLocationDetails, HttpMethod.GET, entity,
                    TuitionClassListResponseWrapper.class);
            var tuitionClassResponseList = Objects.requireNonNull(tuitionClassResponse.getBody()).getData().getLocations();
            Map<String, TuitionClassResponseDto> tuitionClassDetailsMap = new HashMap<>();
            for (TuitionClassResponseDto responseDto : tuitionClassResponseList) {
                tuitionClassDetailsMap.put(responseDto.getTuitionClassId(), responseDto);
            }
            return tuitionClassDetailsMap;
        } catch (ResourceAccessException e) {
            throw new ConnectionException(CONNECTION_EXCEPTION_MESSAGE);
        } catch (HttpClientErrorException e) {
            throw new PaymentException("The requesting data is failed.", e);
        }
    }

    /**
     * Get students payment report for a given month
     *
     * @param month month
     * @param year  year
     * @return PaymentPage
     */
    public Page<Payment> getUserReport(String month, int year) {
        try {
            String paymentMonth = new PaymentMonthDto(month, year).getCombinedDate();
            Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
            return paymentRepository.findByPaymentMonth(pageable, paymentMonth);
        } catch (DataAccessException e) {
            throw new PaymentException("Retrieving the payment reports from database is failed");
        }
    }

    /**
     * Check the exists of a payment
     *
     * @param paymentMonth payment month
     * @param studentId    student id
     * @param paymentId    payment id
     * @return true/ false
     */
    private boolean checkExistsPayment(PaymentMonthDto paymentMonth, String studentId, String paymentId) {
        try {
            var paymentMonthAsString = paymentMonth.getCombinedDate();
            if (paymentId == null) {
                return paymentRepository.existsByPaymentMonthAndStudentIdAndIsDeletedFalse(paymentMonthAsString, studentId);
            } else {
                return paymentRepository
                        .existsByPaymentMonthAndStudentIdAndPaymentIdAndIsDeletedFalse(paymentMonthAsString, studentId, paymentId);
            }
        } catch (DataAccessException e) {
            throw new PaymentException("Checking the existing payment is failed");
        }
    }

    /**
     * Check the existence of the student id
     *
     * @param uri       student service uri
     * @param authToken access token
     * @return true/ false
     */
    private boolean existsStudentId(String uri, String authToken) {
        var headers = new HttpHeaders();
        headers.set(Constants.TOKEN_HEADER, authToken);
        var entity = new HttpEntity<String>(headers);
        var studentResponse = restTemplate.exchange(uri, HttpMethod.GET, entity,
                StudentResponseWrapper.class);
        var statusCode = Objects.requireNonNull(studentResponse.getBody()).getStatusCode();
        return statusCode == SuccessResponseStatus.READ_STUDENT.getStatusCode();
    }
}
