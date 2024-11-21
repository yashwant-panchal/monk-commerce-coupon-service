package com.monk.commerce.coupon.service.exception;

import com.monk.commerce.coupon.service.data.enums.COUPON_TYPE;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Global exception handler. Handles the exceptions
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle data integrity violation exception response entity.
     *
     * @param ex the ex
     * @return the response entity
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "BAD REQUEST");
        if(StringUtils.containsIgnoreCase(ex.getCause().getLocalizedMessage(),"CHK_EXPIRES_GT_CREATED")){
            response.put("details","expiry date should be greater than created_date");
        } else response.put("details",ex.getCause().getLocalizedMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle monk response exceptions response entity.
     *
     * @param ex the ex
     * @return the response entity
     */
    @ExceptionHandler(MonkCommerceException.class)
    public ResponseEntity<Map<String, Object>> handleMonkResponseExceptions(MonkCommerceException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bad Request");
        response.put("details", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle transactional exceptions response entity.
     *
     * @param ex the ex
     * @return the response entity
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionalExceptions(TransactionSystemException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bad Request");
        if(StringUtils.containsIgnoreCase(ex.getMostSpecificCause().getLocalizedMessage(),"Not a valid coupon")){
            response.put("details","Coupon type is not valid. Valid Coupon Types = "+Arrays.asList(COUPON_TYPE.values()));
        } else response.put("details", ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
