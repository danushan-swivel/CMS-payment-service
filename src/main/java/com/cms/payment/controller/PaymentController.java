package com.cms.payment.controller;

import com.cms.payment.domain.entity.Payment;
import com.cms.payment.domain.request.PaymentRequestDto;
import com.cms.payment.domain.request.UpdatePaymentRequestDto;
import com.cms.payment.domain.response.PaymentListResponseDto;
import com.cms.payment.domain.response.PaymentReportListResponseDto;
import com.cms.payment.domain.response.PaymentResponseDto;
import com.cms.payment.enums.ErrorResponseStatus;
import com.cms.payment.enums.SuccessResponseStatus;
import com.cms.payment.exception.InvalidPaymentException;
import com.cms.payment.exception.InvalidStudentException;
import com.cms.payment.exception.PaymentException;
import com.cms.payment.service.PaymentService;
import com.cms.payment.utills.Constants;
import com.cms.payment.wrapper.ErrorResponseWrapper;
import com.cms.payment.wrapper.ResponseWrapper;
import com.cms.payment.wrapper.SuccessResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequestMapping("api/v1/payment")
@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Make new payment
     *
     * @param paymentRequestDto payment request dto
     * @param request authentication request
     * @return Success / Error response
     */
    @PostMapping("")
    public ResponseEntity<ResponseWrapper> makePayment(@RequestBody PaymentRequestDto paymentRequestDto,
                                                       HttpServletRequest request) {
        try {
            if (!paymentRequestDto.isRequiredAvailable()) {
                var response = new ErrorResponseWrapper(ErrorResponseStatus.MISSING_REQUIRED_FIELDS, null);
                log.debug("The required fields {} are missing for make a new payment", paymentRequestDto.toJson());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            String authToken = request.getHeader(Constants.TOKEN_HEADER);
            var payment = paymentService.makePayment(paymentRequestDto, authToken);
            var responseDto = new PaymentResponseDto(payment);
            var successResponse = new SuccessResponseWrapper(SuccessResponseStatus.PAID_SUCCESSFUL, responseDto);
            log.debug("The new payment made successfully");
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        } catch (InvalidStudentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_STUDENT, null);
            log.error("The payment is failed due to invalid student id: {}", paymentRequestDto.getStudentId());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            log.error("The payment is failed for the student id: {} & month: {} ", paymentRequestDto.getPaymentMonth().toJson(),
                    paymentRequestDto.getStudentId());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update existing payment
     *
     * @param updatePaymentRequestDto update payment request dto
     * @param request authentication request
     * @return Success / Error response
     */
    @PutMapping("")
    public ResponseEntity<ResponseWrapper> updatePayment(@RequestBody UpdatePaymentRequestDto updatePaymentRequestDto,
                                                         HttpServletRequest request) {
        try {
            if (!updatePaymentRequestDto.isRequiredAvailable()) {
                var response = new ErrorResponseWrapper(ErrorResponseStatus.MISSING_REQUIRED_FIELDS, null);
                log.debug("The required fields {} are missing for update the payment", updatePaymentRequestDto.toJson());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            String authToken = request.getHeader(Constants.TOKEN_HEADER);
            var payment = paymentService.updatePayment(updatePaymentRequestDto, authToken);
            var responseDto = new PaymentResponseDto(payment);
            var successResponse = new SuccessResponseWrapper(SuccessResponseStatus.PAYMENT_UPDATED, responseDto);
            log.debug("The payment is updated successfully for payment id: {}", updatePaymentRequestDto.getPaymentId());
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        } catch (InvalidStudentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_STUDENT, null);
            log.error("The payment is failed due to invalid student id: {} for payment id: {}",
                    updatePaymentRequestDto.getStudentId(), updatePaymentRequestDto.getPaymentId());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (InvalidPaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_PAYMENT, null);
            log.error("The payment is failed due to invalid payment id: {}", updatePaymentRequestDto.getPaymentId());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            log.error("The updating payment is failed for {}", updatePaymentRequestDto.toJson());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all payment details which mapped with student and location details
     *
     * @param request authentication request
     * @return Success / Error response
     */
    @GetMapping("")
    public ResponseEntity<ResponseWrapper> getAllPayment(HttpServletRequest request) {
        try {
            String authToken = request.getHeader(Constants.TOKEN_HEADER);
            Page<Payment> paymentPage = paymentService.getAllPayment();
            var studentMap = paymentService.getStudentsDetails(authToken);
            var locationMap = paymentService.getTuitionClassDetails(authToken);
            var response = new PaymentListResponseDto(paymentPage, studentMap, locationMap);
            var successResponseWrapper = new SuccessResponseWrapper(SuccessResponseStatus.READ_LIST_PAYMENT, response);
            log.debug("Retrieve all payment details successfully");
            return new ResponseEntity<>(successResponseWrapper, HttpStatus.OK);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            log.error("Retrieve the payment details is failed");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all payment details which belongs a student
     *
     * @param studentId student id
     * @param request authentication request
     * @return Success / Error response
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResponseWrapper> getAllPaymentsByStudentId(@PathVariable String studentId,
                                                                     HttpServletRequest request) {
        try {
            String authToken = request.getHeader(Constants.TOKEN_HEADER);
            Page<Payment> paymentPage = paymentService.getPaymentsByStudentId(studentId);
            var studentMap = paymentService.getStudentsDetails(authToken);
            var locationMap = paymentService.getTuitionClassDetails(authToken);
            var response = new PaymentListResponseDto(paymentPage, studentMap, locationMap);
            var successResponseWrapper = new SuccessResponseWrapper(SuccessResponseStatus.READ_LIST_PAYMENT, response);
            log.debug("Retrieve all payment details for the student id: {}", studentId);
            return new ResponseEntity<>(successResponseWrapper, HttpStatus.OK);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            log.error("Retrieve all payment details for the student id: {} is failed", studentId);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a existing payment
     *
     * @param paymentId payment id
     * @return Success / Error response
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ResponseWrapper> deletePayment(@PathVariable String paymentId) {
        try {
            paymentService.deletePayment(paymentId);
            var successResponseWrapper = new SuccessResponseWrapper(SuccessResponseStatus.PAYMENT_DELETED, null);
            log.debug("Payment is deleted successfully for the payment id: {}", paymentId);
            return new ResponseEntity<>(successResponseWrapper, HttpStatus.OK);
        } catch (InvalidPaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_PAYMENT, null);
            log.error("The deleting payment is failed due to invalid payment id: {}", paymentId);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            log.error("The deleting payment is failed for payment id: {}", paymentId);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get the paid and unpaid student details on a particular month
     *
     * @param month month
     * @param year year
     * @param request authentication request
     * @return Success / Error response
     */
    @GetMapping("/student/report/{month}/{year}")
    public ResponseEntity<ResponseWrapper> getUserReport(@PathVariable String month, @PathVariable int year,
                                                            HttpServletRequest request) {
        try {
            String authToken = request.getHeader(Constants.TOKEN_HEADER);
            Page<Payment> paymentPage = paymentService.getUserReport(month, year);
            var studentMap = paymentService.getStudentsDetails(authToken);
            var locationMap = paymentService.getTuitionClassDetails(authToken);
            var response = new PaymentReportListResponseDto(paymentPage, studentMap, locationMap);
            var successResponseWrapper = new SuccessResponseWrapper(SuccessResponseStatus.READ_STUDENT_PAYMENT_REPORT, response);
            log.debug("Month based payment report is generated successfully for the month: {} year: {}", month, year);
            return new ResponseEntity<>(successResponseWrapper, HttpStatus.OK);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            log.error("Generating month based payment report is failed for the month: {} year: {}", month, year);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
