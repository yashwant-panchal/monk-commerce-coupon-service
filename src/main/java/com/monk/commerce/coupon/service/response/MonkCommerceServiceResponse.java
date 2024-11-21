package com.monk.commerce.coupon.service.response;

import lombok.Builder;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * The type Monk commerce service response.
 *
 * @param <T> the type parameter
 */
@Builder
public class MonkCommerceServiceResponse<T> {
    private T body;

    /**
     * Return created response entity.
     *
     * @return the response entity
     */
    public ResponseEntity<T> returnCreatedResponse(){
        return ResponseEntity
                .status(HttpStatusCode.valueOf(201))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * Return accepted response response entity.
     *
     * @return the response entity
     */
    public ResponseEntity<T> returnAcceptedResponse(){
        return ResponseEntity
                .accepted()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * Return ok response response entity.
     *
     * @return the response entity
     */
    public ResponseEntity<T> returnOkResponse(){
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
