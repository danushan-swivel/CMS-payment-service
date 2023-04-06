package com.cms.payment.controller;

import com.cms.payment.domain.entity.Payment;
import com.cms.payment.domain.request.PaymentRequestDto;
import com.cms.payment.domain.request.UpdatePaymentRequestDto;
import com.cms.payment.domain.response.PaymentListResponseDto;
import com.cms.payment.domain.response.PaymentReportListResponseDto;
import com.cms.payment.domain.response.PaymentResponseDto;
import com.cms.payment.enums.ErrorResponseStatus;
import com.cms.payment.enums.SuccessResponseStatus;
import com.cms.payment.service.PaymentService;
import com.cms.payment.utills.Constants;
import com.cms.payment.wrapper.ResponseWrapper;
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
public class PaymentController extends BaseController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Make new payment
     *
     * @param paymentRequestDto payment request dto
     * @param request           authentication request
     * @return Success / Error response
     */
    @PostMapping("")
    public ResponseEntity<ResponseWrapper> makePayment(@RequestBody PaymentRequestDto paymentRequestDto,
                                                       HttpServletRequest request) {
        if (!paymentRequestDto.isRequiredAvailable()) {
            log.debug("The required fields {} are missing for make a new payment", paymentRequestDto.toJson());
            return getErrorResponse(ErrorResponseStatus.MISSING_REQUIRED_FIELDS);
        }
        String authToken = request.getHeader(Constants.TOKEN_HEADER);
        var payment = paymentService.makePayment(paymentRequestDto, authToken);
        var responseDto = new PaymentResponseDto(payment);
        log.debug("The new payment made successfully");
        return getSuccessResponse(SuccessResponseStatus.PAID_SUCCESSFUL, responseDto, HttpStatus.CREATED);
    }

    /**
     * Update existing payment
     *
     * @param updatePaymentRequestDto update payment request dto
     * @param request                 authentication request
     * @return Success / Error response
     */
    @PutMapping("")
    public ResponseEntity<ResponseWrapper> updatePayment(@RequestBody UpdatePaymentRequestDto updatePaymentRequestDto,
                                                         HttpServletRequest request) {
        if (!updatePaymentRequestDto.isRequiredAvailable()) {
            log.debug("The required fields {} are missing for update the payment", updatePaymentRequestDto.toJson());
            return getErrorResponse(ErrorResponseStatus.MISSING_REQUIRED_FIELDS);
        }
        String authToken = request.getHeader(Constants.TOKEN_HEADER);
        var payment = paymentService.updatePayment(updatePaymentRequestDto, authToken);
        var responseDto = new PaymentResponseDto(payment);
        log.debug("The payment is updated successfully for payment id: {}", updatePaymentRequestDto.getPaymentId());
        return getSuccessResponse(SuccessResponseStatus.PAYMENT_UPDATED, responseDto, HttpStatus.OK);
    }

    /**
     * Get all payment details which mapped with student and location details
     *
     * @param request authentication request
     * @return Success / Error response
     */
    @GetMapping("")
    public ResponseEntity<ResponseWrapper> getAllPayment(HttpServletRequest request) {
        String authToken = request.getHeader(Constants.TOKEN_HEADER);
        Page<Payment> paymentPage = paymentService.getAllPayment();
        var studentMap = paymentService.getStudentsDetails(authToken);
        var locationMap = paymentService.getTuitionClassDetails(authToken);
        var response = new PaymentListResponseDto(paymentPage, studentMap, locationMap);
        log.debug("Retrieve all payment details successfully");
        return getSuccessResponse(SuccessResponseStatus.READ_LIST_PAYMENT, response, HttpStatus.OK);
    }

    /**
     * Get all payment details which belongs a student
     *
     * @param studentId student id
     * @param request   authentication request
     * @return Success / Error response
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResponseWrapper> getAllPaymentsByStudentId(@PathVariable String studentId,
                                                                     HttpServletRequest request) {
        String authToken = request.getHeader(Constants.TOKEN_HEADER);
        Page<Payment> paymentPage = paymentService.getPaymentsByStudentId(studentId);
        var studentMap = paymentService.getStudentsDetails(authToken);
        var locationMap = paymentService.getTuitionClassDetails(authToken);
        var response = new PaymentListResponseDto(paymentPage, studentMap, locationMap);
        log.debug("Retrieve all payment details for the student id: {}", studentId);
        return getSuccessResponse(SuccessResponseStatus.READ_LIST_PAYMENT, response, HttpStatus.OK);
    }

    /**
     * Delete a existing payment
     *
     * @param paymentId payment id
     * @return Success / Error response
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ResponseWrapper> deletePayment(@PathVariable String paymentId) {
        paymentService.deletePayment(paymentId);
        log.debug("Payment is deleted successfully for the payment id: {}", paymentId);
        return getSuccessResponse(SuccessResponseStatus.PAYMENT_DELETED, null, HttpStatus.OK);
    }

    /**
     * Get the paid and unpaid student details on a particular month
     *
     * @param month   month
     * @param year    year
     * @param request authentication request
     * @return Success / Error response
     */
    @GetMapping("/student/report/{month}/{year}")
    public ResponseEntity<ResponseWrapper> getUserReport(@PathVariable String month, @PathVariable int year,
                                                         HttpServletRequest request) {
        String authToken = request.getHeader(Constants.TOKEN_HEADER);
        Page<Payment> paymentPage = paymentService.getUserReport(month, year);
        var studentMap = paymentService.getStudentsDetails(authToken);
        var locationMap = paymentService.getTuitionClassDetails(authToken);
        var response = new PaymentReportListResponseDto(paymentPage, studentMap, locationMap);
        log.debug("Month based payment report is generated successfully for the month: {} year: {}", month, year);
        return getSuccessResponse(SuccessResponseStatus.READ_STUDENT_PAYMENT_REPORT, response, HttpStatus.OK);
    }
}
