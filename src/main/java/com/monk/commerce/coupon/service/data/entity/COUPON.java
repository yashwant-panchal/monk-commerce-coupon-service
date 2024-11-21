package com.monk.commerce.coupon.service.data.entity;

import com.monk.commerce.coupon.service.data.enums.COUPON_TYPE;
import com.monk.commerce.coupon.service.exception.MonkCommerceException;
import com.monk.commerce.coupon.service.validator.CartUtil;
import com.monk.commerce.coupon.service.validator.ValidateEnum;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.validator.constraints.Range;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Slf4j
public class COUPON {
    /**
     * Unique ID of coupon
     */
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Type of coupon
     */
    @ValidateEnum(enumClass = COUPON_TYPE.class, message = "Not a valid coupon type")
    private String coupon_type;

    /**
     * Created time stamp of Coupon
     */
    @Column(name = "created_at", updatable = false)
    private Timestamp created_at;


    /**
     * Time stamp when coupon was updated last time
     */
    private Timestamp updated_at;

    /**
     * Expiry time of coupon
     */
    @Valid
    private Timestamp expires_at;

    /**
     * Discount percentage provided by Coupon. Value should be >= 1 and <= 99
     */
    private int discount_percentage;

    /**
     * Conditions on which this coupon can be applicable
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String,Object> conditions;

    /**
     * Coupon description
     */
    @Builder.Default private String description = "Discount Coupon";

    /**
     * Set created date/ updated date
     * and validates the coupon conditions
     */
    @PrePersist
    protected void onCreate(){
        this.created_at = Timestamp.valueOf(LocalDateTime.now());
        this.updated_at = Timestamp.valueOf(LocalDateTime.now());
        validateCouponCondition();
    }

    @PreUpdate
    protected void onUpdate(){
        this.updated_at = Timestamp.valueOf(LocalDateTime.now());
        validateCouponCondition();
    }

    /**
     * Validate coupon condition based on type
     * Todo : Make it generic so that new condition should not be added when a new Coupon type is creating
     */
    private void validateCouponCondition() {
        COUPON_TYPE couponType = COUPON_TYPE.valueOf(coupon_type);
        switch (couponType){
            case PRODUCT_WISE:
                validateProductWiseCoupon();
                return;
            case CART_WISE:
                validateCartWiseCoupon();
                return;
            case BX_GY:
                validateBxGyCoupon();
        }
    }

    private void validateProductWiseCoupon(){
        String reqCond = """
                            Please provide following properties
                            1. product : Product on which coupon is applicable.
                         """;

        Object product = conditions == null ? null : conditions.get("product");

        if(product == null){
            throw new MonkCommerceException(reqCond);
        }
        validateDiscountPercentage();
    }

    private void validateCartWiseCoupon(){
        String reqCond = """
                             Please provide following properties
                             1. minValueToApplyDiscount : min Value of cart to apply discount.
                                 Value should be in Double format and > 0.0
                         """;

        Object minValueToApplyDiscount = conditions == null ? null : conditions.get("minValueToApplyDiscount");
        log.info("MinValueToApplyDiscount : {}",minValueToApplyDiscount);
        if (!(minValueToApplyDiscount instanceof Double)) {
            throw new MonkCommerceException(reqCond);
        }
        validateDiscountPercentage();
    }

    private void validateBxGyCoupon(){
        String reqCond = """
                         Please provide following properties
                         1. buy_products : List of products that needed to be purchased by customer
                         2. get_products : List of products that will be provided free to customer
                         3. min_buy : Min number of product that should be purchased from buy_product list
                         4. get_free : Number of product that will be provided free to customer
                         5. repetition_limit : How many items can be avail free.
                           - E.g. If the repetition limit is 3, the coupon can be applied 3 times.
                                - If the cart has 6 products from the “buy_products” list and 3 products from the
                                “get_products” list, the coupon can be applied 3 times. I.e. for b2g1, buying 6
                                products from [X, Y, Z] would result in getting 3 products from [A, B, C] for
                                free.
                                - If the cart has products [X, X, X, Y, Y, Y] (6 items from the “buy” array)
                                and products [A, B, C] or [A,B], then [A, B, and C] or [A,B] would be free.
                          """;

        Map<String,Object> buyProduct = (Map<String, Object>) conditions.get("buy_product");
        Map<String,Object> getProduct = (Map<String, Object>) conditions.get("get_product");

        if(conditions==null || buyProduct == null || getProduct== null
                || !(conditions.get("repetition_limit") instanceof Integer)){
            throw new MonkCommerceException(reqCond);
        }

        Product buy = CartUtil.convertMapToProduct(buyProduct);
        Product get = CartUtil.convertMapToProduct(getProduct);
        if(StringUtils.isAnyBlank(buy.getName(),get.getName())){
            throw new MonkCommerceException("Provide proper product names");
        }
        if(buy.getQuantity()<=0 || get.getQuantity()<=0){
            throw new MonkCommerceException("Select proper quantity of buy and get products");
        }
    }

    private void validateDiscountPercentage(){
        if(this.discount_percentage <1 || this.discount_percentage >99){
            throw new MonkCommerceException("discount_percentage should be >= 1 && <= 99");
        }
    }
}
