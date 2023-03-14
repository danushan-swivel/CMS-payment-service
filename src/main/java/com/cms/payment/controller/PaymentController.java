package com.cms.payment.controller;

import com.cms.payment.domain.entity.Payment;
import com.cms.payment.domain.request.PaymentRequestDto;
import com.cms.payment.domain.request.UpdatePaymentRequestDto;
import com.cms.payment.domain.response.PaymentListResponseDto;
import com.cms.payment.domain.response.PaymentResponseDto;
import com.cms.payment.enums.ErrorResponseStatus;
import com.cms.payment.enums.SuccessResponseStatus;
import com.cms.payment.exception.InvalidPaymentException;
import com.cms.payment.exception.PaymentException;
import com.cms.payment.exception.InvalidStudentException;
import com.cms.payment.service.PaymentService;
import com.cms.payment.utills.Constants;
import com.cms.payment.wrapper.ErrorResponseWrapper;
import com.cms.payment.wrapper.ResponseWrapper;
import com.cms.payment.wrapper.SuccessResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("api/v1/payment")
@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("")
    public ResponseEntity<ResponseWrapper> makePayment(@RequestBody PaymentRequestDto paymentRequestDto,
                                                       HttpServletRequest request) {
        try {
            if (!paymentRequestDto.isRequiredAvailable()) {
                var response = new ErrorResponseWrapper(ErrorResponseStatus.MISSING_REQUIRED_FIELDS, null);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            String authToken = request.getHeader(Constants.TOKEN_HEADER);
            var payment = paymentService.makePayment(paymentRequestDto, authToken);
            var responseDto = new PaymentResponseDto(payment);
            var successResponse = new SuccessResponseWrapper(SuccessResponseStatus.PAID_SUCCESSFUL, responseDto);
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        } catch (InvalidStudentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_STUDENT, null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("")
    public ResponseEntity<ResponseWrapper> updatePayment(@RequestBody UpdatePaymentRequestDto updatePaymentRequestDto,
                                                         HttpServletRequest request) {
        try {
            if (!updatePaymentRequestDto.isRequiredAvailable()) {
                var response = new ErrorResponseWrapper(ErrorResponseStatus.MISSING_REQUIRED_FIELDS, null);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            String authToken = request.getHeader(Constants.TOKEN_HEADER);
            var payment = paymentService.updatePayment(updatePaymentRequestDto, authToken);
            var responseDto = new PaymentResponseDto(payment);

            var successResponse = new SuccessResponseWrapper(SuccessResponseStatus.PAYMENT_UPDATED, responseDto);
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        } catch (InvalidStudentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_STUDENT, null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (InvalidPaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_PAYMENT, null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("")
    public ResponseEntity<ResponseWrapper> getAllPayment(HttpServletRequest request) {
        try {
            String authToken = request.getHeader(Constants.TOKEN_HEADER);
            Page<Payment> paymentPage = paymentService.getAllPayment();
            var studentMap = paymentService.getStudentsDetails(authToken);
            var locationMap = paymentService.getTuitionClassDetails(authToken);
            var response = new PaymentListResponseDto(paymentPage, studentMap, locationMap);
            var successResponseWrapper = new SuccessResponseWrapper(SuccessResponseStatus.READ_LIST_PAYMENT, response);
            return new ResponseEntity<>(successResponseWrapper, HttpStatus.OK);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResponseWrapper> getAllPaymentsByStudentId(@PathVariable String studentId) {
        try {
            Page<Payment> paymentPage = paymentService.getPaymentsByStudentId(studentId);
            var response = new PaymentListResponseDto(paymentPage, null, null);
            var successResponseWrapper = new SuccessResponseWrapper(SuccessResponseStatus.READ_LIST_PAYMENT, response);
            return new ResponseEntity<>(successResponseWrapper, HttpStatus.OK);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ResponseWrapper> getPaymentById(@PathVariable String paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            var response = new PaymentResponseDto(payment);
            var successResponseWrapper = new SuccessResponseWrapper(SuccessResponseStatus.READ_PAYMENT, response);
            return new ResponseEntity<>(successResponseWrapper, HttpStatus.OK);
        } catch (InvalidPaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_PAYMENT, null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ResponseWrapper> deletePayment(@PathVariable String paymentId) {
        try {
            paymentService.deletePayment(paymentId);
            var successResponseWrapper = new SuccessResponseWrapper(SuccessResponseStatus.PAYMENT_DELETED, null);
            return new ResponseEntity<>(successResponseWrapper, HttpStatus.OK);
        } catch (InvalidPaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_PAYMENT, null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (PaymentException e) {
            var response = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
