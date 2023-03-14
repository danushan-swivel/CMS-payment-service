package com.cms.payment.service;

import com.cms.payment.domain.entity.Payment;
import com.cms.payment.domain.request.PaymentRequestDto;
import com.cms.payment.domain.request.UpdatePaymentRequestDto;
import com.cms.payment.domain.response.TuitionClassResponseDto;
import com.cms.payment.domain.response.StudentResponseDto;
import com.cms.payment.exception.InvalidPaymentException;
import com.cms.payment.exception.PaymentException;
import com.cms.payment.exception.InvalidStudentException;
import com.cms.payment.exception.UnavailableException;
import com.cms.payment.repository.PaymentRepository;
import com.cms.payment.wrapper.LocationListResponseWrapper;
import com.cms.payment.wrapper.StudentListResponseWrapper;
import com.cms.payment.wrapper.StudentResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private static final int PAGE = 0;
    private static final int SIZE = 10;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final String getStudentByIdUrl;
    private final String getAllStudentDetails;
    private final String getAllLocationDetails;
    private static final String DEFAULT_SORT = "updated_at";
    private static final String STUDENT_ID_REPLACE_PHRASE = "##STUDENT-ID##";

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

    public Payment makePayment(PaymentRequestDto paymentRequestDto, String authToken) {
        try {
            Payment payment = new Payment(paymentRequestDto);
            var headers = new HttpHeaders();
            headers.set("access_token", authToken);
            var entity = new HttpEntity<String>(headers);
            String uri = getStudentByIdUrl.replace(STUDENT_ID_REPLACE_PHRASE, paymentRequestDto.getStudentId());
            var studentResponse = restTemplate.exchange(uri, HttpMethod.GET, entity,
                    StudentResponseWrapper.class);
            if (studentResponse.getBody().getData() == null) {
                throw new InvalidStudentException("Invalid student Id : " + paymentRequestDto.getStudentId());
            }
             return paymentRepository.save(payment);
        } catch (ResourceAccessException e) {
            throw new UnavailableException("The requested resource couldn't access due to unavailability");
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == HttpStatus.BAD_REQUEST.value()) {
                throw new InvalidStudentException("Invalid student Id : " + paymentRequestDto.getStudentId());
            }
            throw new PaymentException("Validating student identity is failed", e);
        }
        catch (DataAccessException e) {
            throw new PaymentException("Saving payment details into database is failed.", e);
        }
    }

    public Payment getPaymentById(String paymentId) {
        try {
            Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
            if (optionalPayment.isEmpty()) {
                throw new InvalidPaymentException("Invalid payment Id : "+ paymentId);
            }
            return optionalPayment.get();
        } catch (DataAccessException e) {
            throw new PaymentException("Retrieving payment from database is failed for "+ paymentId);
        }
    }

    public Payment updatePayment(UpdatePaymentRequestDto updatePaymentRequestDto, String authToken) {
        try {
            Payment paymentFromDB = getPaymentById(updatePaymentRequestDto.getPaymentId());
            var headers = new HttpHeaders();
            headers.set("access_token", authToken);
            var entity = new HttpEntity<String>(headers);
            String uri = getStudentByIdUrl.replace(STUDENT_ID_REPLACE_PHRASE, paymentFromDB.getStudentId());
            var studentResponse = restTemplate.exchange(uri, HttpMethod.GET, entity,
                    StudentResponseWrapper.class);
            if (studentResponse.getBody().getData() == null) {
                throw new InvalidStudentException("Invalid student Id : " + paymentFromDB.getStudentId());
            }
            paymentFromDB.update(updatePaymentRequestDto);
            return paymentFromDB;
        } catch (DataAccessException e) {
            throw new PaymentException("Updating payment is failed for "+ updatePaymentRequestDto.getPaymentId());
        }
    }

    public void deletePayment(String paymentId) {
        try {
            Payment paymentFromDB = getPaymentById(paymentId);
            paymentFromDB.setDeleted(true);
            paymentFromDB.setUpdatedAt(new Date(System.currentTimeMillis()));
            paymentRepository.save(paymentFromDB);
        } catch (DataAccessException e) {
            throw new PaymentException("Deleting payment from database is failed for "+ paymentId);
        }
    }

    public Page<Payment> getAllPayment() {
        try {
            Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
            return paymentRepository.findAll(pageable);
        } catch (DataAccessException e) {
            throw new PaymentException("Retrieving Payment list from database is failed.");
        }
    }

    public Page<Payment> getPaymentsByStudentId(String studentId) {
        try {
            Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
            return paymentRepository.findByStudentId(pageable, studentId);
        } catch (DataAccessException e) {
            throw new PaymentException("Retrieving Payment list for student id: " + studentId
                    + " from database is failed.");
        }
    }

    public Map<String, StudentResponseDto> getStudentsDetails(String authToken) {
        try{
            var headers = new HttpHeaders();
            headers.set("access_token", authToken);
            var entity = new HttpEntity<String>(headers);
            var studentResponse = restTemplate.exchange(getAllStudentDetails, HttpMethod.GET, entity,
                    StudentListResponseWrapper.class);
            var studentResponseList = studentResponse.getBody().getData().getStudents();
            Map<String, StudentResponseDto> studentDetailsMap = new HashMap<>();
            for (StudentResponseDto responseDto: studentResponseList) {
                studentDetailsMap.put(responseDto.getStudentId(), responseDto);
            }
            return studentDetailsMap;
        } catch (ResourceAccessException e) {
            throw new UnavailableException("The requested resource couldn't access due to unavailability");
        } catch (HttpClientErrorException e) {
            throw new PaymentException("The requesting data is failed.", e);
        }
    }

    public Map<String, TuitionClassResponseDto> getTuitionClassDetails(String authToken) {
        try{
            var headers = new HttpHeaders();
            headers.set("access_token", authToken);
            var entity = new HttpEntity<String>(headers);
            var tuitionClassResponse = restTemplate.exchange(getAllLocationDetails, HttpMethod.GET, entity,
                    LocationListResponseWrapper.class);
            var tuitionClassResponseList = tuitionClassResponse.getBody().getData().getLocations();
            Map<String, TuitionClassResponseDto> tuitionClassDetailsMap = new HashMap<>();
            for (TuitionClassResponseDto responseDto: tuitionClassResponseList) {
                tuitionClassDetailsMap.put(responseDto.getTuitionClassId(), responseDto);
            }
            return tuitionClassDetailsMap;
        } catch (ResourceAccessException e) {
            throw new UnavailableException("The requested resource couldn't access due to unavailability");
        } catch (HttpClientErrorException e) {
            throw new PaymentException("The requesting data is failed.", e);
        }
    }
}
