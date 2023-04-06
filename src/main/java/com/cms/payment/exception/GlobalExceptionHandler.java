package com.cms.payment.exception;

import com.cms.payment.enums.ErrorResponseStatus;
import com.cms.payment.wrapper.ErrorResponseWrapper;
import com.cms.payment.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * This method handle connection failed exception response
     *
     * @param exception microservices connection failed
     * @return ErrorResponse/BadRequest
     */
    @ExceptionHandler(ConnectionException.class)
    public ResponseEntity<ResponseWrapper> connectionException(ConnectionException exception) {
        var wrapper = new ErrorResponseWrapper(ErrorResponseStatus.INTER_CONNECTION_FAILED, HttpStatus.BAD_REQUEST);
        log.error("The connection failed between micro services. Error message: {}", exception.getMessage());
        return new ResponseEntity<>(wrapper, HttpStatus.BAD_REQUEST);
    }
    /**
     * This method handle invalid payment exception response
     *
     * @param exception invalid payment exception
     * @return ErrorResponse/BadRequest
     */
    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ResponseWrapper> invalidPaymentException(InvalidPaymentException exception) {
        var wrapper = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_PAYMENT, HttpStatus.BAD_REQUEST);
        log.error("The given payment id is not exists. Error message: {}", exception.getMessage());
        return new ResponseEntity<>(wrapper, HttpStatus.BAD_REQUEST);
    }
    /**
     * This method handle payment already exists exception response
     *
     * @param exception payment already exists exception
     * @return ErrorResponse/BadRequest
     */
    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ResponseWrapper> paymentAlreadyMade(PaymentAlreadyExistsException exception) {
        var wrapper = new ErrorResponseWrapper(ErrorResponseStatus.ALREADY_PAID, HttpStatus.BAD_REQUEST);
        log.error("The payment already made. Error message: {}", exception.getMessage());
        return new ResponseEntity<>(wrapper, HttpStatus.BAD_REQUEST);
    }
    /**
     * This method handle failed student exception response
     *
     * @param exception invalid student exception
     * @return ErrorResponse/BadRequest
     */
    @ExceptionHandler(InvalidStudentException.class)
    public ResponseEntity<ResponseWrapper> invalidStudentException(InvalidStudentException exception) {
        var wrapper = new ErrorResponseWrapper(ErrorResponseStatus.INVALID_STUDENT, HttpStatus.BAD_REQUEST);
        log.error("The retrieving the student is failed due to invalid student id. Error message: {}",
                exception.getMessage());
        return new ResponseEntity<>(wrapper, HttpStatus.BAD_REQUEST);
    }
    /**
     * This method handle payment exception response
     *
     * @param exception payment exception
     * @return ErrorResponse/InternalServerError
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ResponseWrapper> paymentException(PaymentException exception) {
        var wrapper = new ErrorResponseWrapper(ErrorResponseStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        log.error("The payment service is failed. Error message: {}", exception.getMessage());
        return new ResponseEntity<>(wrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
