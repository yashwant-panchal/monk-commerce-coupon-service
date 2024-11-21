package com.monk.commerce.coupon.service.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The enum Coupon type. Add new value to introduce new coupon type
 */
@Getter
@AllArgsConstructor
public enum COUPON_TYPE {
    /**
     * Apply a discount to the entire cart if the total amount exceeds a certain threshold.
     */
    CART_WISE,

    /**
     * Apply a discount to specific products.
     */
    PRODUCT_WISE,

    /**
     * Buy minimum number of Product X, get a Product Y
     */
    BX_GY
}
