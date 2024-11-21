package com.monk.commerce.coupon.service.service.api;

import com.monk.commerce.coupon.service.data.entity.COUPON;
import com.monk.commerce.coupon.service.data.entity.Cart;
import com.monk.commerce.coupon.service.exception.MonkCommerceException;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * The interface Coupon executor.
 *
 * @param <T> the type parameter
 * Implement this interface to apply any new type of Coupon
 */
public interface CouponExecutor<T> {
    /**
     * Apply coupon to T.
     *
     * @param cart   the cart
     * @param coupon the coupon
     * @return the T
     */
    T applyCoupon(T cart, COUPON coupon);

    /**
     * Is coupon applicable on cart boolean.
     *
     * @param coupon the coupon
     * @param cart   the cart
     * @return the boolean
     */
    boolean isCouponApplicableOnCart(COUPON coupon, T cart);
}
