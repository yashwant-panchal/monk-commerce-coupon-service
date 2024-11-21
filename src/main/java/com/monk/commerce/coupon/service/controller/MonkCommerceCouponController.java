package com.monk.commerce.coupon.service.controller;

import com.monk.commerce.coupon.service.data.entity.COUPON;
import com.monk.commerce.coupon.service.data.entity.Cart;
import com.monk.commerce.coupon.service.data.repository.CouponRepository;
import com.monk.commerce.coupon.service.exception.MonkCommerceException;
import com.monk.commerce.coupon.service.response.MonkCommerceServiceResponse;
import com.monk.commerce.coupon.service.service.api.CouponExecutor;
import com.monk.commerce.coupon.service.validator.CartUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Mon commerce coupon controller.
 */
@RestController
@Validated
@Slf4j
public class MonkCommerceCouponController {
    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private Map<String, CouponExecutor<?>> couponExecutorBeanMap;

    /**
     * Retrieve all coupons.
     *
     * @return the List of all Coupons present in DB
     */
    @RequestMapping(value = "/coupons", method = RequestMethod.GET)
    public ResponseEntity<List<COUPON>> listAllCoupons() {
        return MonkCommerceServiceResponse
                .<List<COUPON>>builder()
                .body(couponRepository.listAllCouponsInDB())
                .build()
                .returnOkResponse();
    }

    /**
     * Create a new coupon.
     *
     * @param coupon the coupon
     * @return the Created Coupon
     */
    @RequestMapping(value = "/coupons", method = RequestMethod.POST)
    public ResponseEntity<COUPON> createCoupon(@RequestBody COUPON coupon) {
        return MonkCommerceServiceResponse
                .<COUPON>builder()
                .body(couponRepository.save(coupon))
                .build()
                .returnCreatedResponse();

    }

    /**
     * Retrieve a specific coupon by its ID.
     *
     * @param id the id
     * @return the Coupon with Given ID else returns null
     */
    @RequestMapping(value = "/coupons/{id}", method = RequestMethod.GET)
    public ResponseEntity<COUPON> getCouponWithByItsId(@PathVariable String id) {
        return MonkCommerceServiceResponse.<COUPON>builder()
                .body(couponRepository.getCouponById(id))
                .build()
                .returnOkResponse();
    }

    /**
     * Update a specific coupon by its ID.
     *
     * @param id the id
     * @return Update Coupon in DB
     */
    @RequestMapping(value = "/coupons/{id}", method = RequestMethod.PUT)
    public ResponseEntity<COUPON> updateCouponWithByItsId(@PathVariable String id, @Validated @RequestBody COUPON updatedCoupon) {
        return MonkCommerceServiceResponse.<COUPON>builder()
                .body(couponRepository.updateCouponById(id, updatedCoupon))
                .build()
                .returnAcceptedResponse();
    }

    /**
     * Delete a specific coupon by its ID.
     *
     * @param id the id
     * @return Deleted Coupon
     */
    @RequestMapping(value = "/coupons/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteCouponWithByItsId(@PathVariable String id) {
        couponRepository.deleteById(id);
        return MonkCommerceServiceResponse.<String>builder()
                .body("DELETION SUCCESS")
                .build()
                .returnAcceptedResponse();
    }

    /**
     * Fetch all applicable coupons for a given cart and
     * calculate the total discount that will be applied by each coupon.
     *
     * @param cart the cart
     * @return the List of applicable coupon and discount on each coupon
     */
    @RequestMapping(value = "/applicable-coupons", method = RequestMethod.POST)
    public ResponseEntity<List<Map<String,Object>>> fetchAllApplicableCouponWithGivenCart(@RequestBody Cart cart) {
        CartUtil.validateProducts(cart.getProducts());
        List<COUPON> availableCoupons = couponRepository.listAllCouponsInDB();
        List<Map<String,Object>> applicableCouponsWithDiscount = new ArrayList<>();
        for(COUPON coupon: availableCoupons){
            if(coupon.getExpires_at()!=null && coupon.getExpires_at().after(Timestamp.valueOf(LocalDateTime.now()))){
                CouponExecutor<Cart> couponExecutor = (CouponExecutor<Cart>) couponExecutorBeanMap.get(coupon.getCoupon_type());
                if(couponExecutor.isCouponApplicableOnCart(coupon,cart)){
                    applicableCouponsWithDiscount.add(
                            Map.of("Coupon",coupon,"total discount available",couponExecutor.applyCoupon(cart,coupon).getAvailableDiscount())
                    );
                }
            }
        }
        return MonkCommerceServiceResponse.<List<Map<String,Object>>>builder()
                .body(applicableCouponsWithDiscount)
                .build()
                .returnOkResponse();
    }

    /**
     * Apply a specific coupon to the cart and return the
     * updated cart with discounted prices for each item.
     *
     * @param id the id
     * @return the response entity
     */
    @RequestMapping(value = "/apply-coupon/{id}", method = RequestMethod.POST)
    public ResponseEntity<Cart> applyCouponToCart(@PathVariable String id, @RequestBody Cart cart) {
        CartUtil.validateProducts(cart.getProducts());
        COUPON coupon = couponRepository.getCouponById(id);
        if(coupon==null){
            throw new MonkCommerceException("Invalid COUPON Id : "+id+", Coupon not found!");
        }

        CouponExecutor<Cart> couponExecutor = (CouponExecutor<Cart>) couponExecutorBeanMap.get(coupon.getCoupon_type());

        if(!couponExecutor.isCouponApplicableOnCart(coupon,cart)){
            throw new MonkCommerceException("This coupon is not applicable on present cart!!!");
        }
        return MonkCommerceServiceResponse
                .<Cart>builder()
                .body(couponExecutor.applyCoupon(cart,coupon))
                .build()
                .returnAcceptedResponse();
    }
}
