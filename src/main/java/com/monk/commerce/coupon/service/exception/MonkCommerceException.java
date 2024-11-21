package com.monk.commerce.coupon.service.exception;

import lombok.Builder;

import java.util.List;


/**
 * The type Monk commerce exception. Class to throw the custom exceptions
 */
public class MonkCommerceException extends RuntimeException {
    /**
     * Instantiates a new Monk commerce exception.
     *
     * @param message the message
     */
    public MonkCommerceException(String message){
        super(message);
    }
}
