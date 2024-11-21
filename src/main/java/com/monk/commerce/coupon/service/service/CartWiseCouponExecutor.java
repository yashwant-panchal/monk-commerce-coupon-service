package com.monk.commerce.coupon.service.service;

import com.monk.commerce.coupon.service.data.entity.COUPON;
import com.monk.commerce.coupon.service.data.entity.Cart;
import com.monk.commerce.coupon.service.data.entity.Product;
import com.monk.commerce.coupon.service.service.api.CouponExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component("CART_WISE")
@Slf4j
public class CartWiseCouponExecutor implements CouponExecutor<Cart> {
    @Override
    public Cart applyCoupon(Cart cart, COUPON coupon) {
        if(isCouponApplicableOnCart(coupon,cart)){
            log.info("Applying CART_WISE coupon of ID : {} on cart",coupon.getId());
            calculateFinalPriceOfCart(cart,coupon.getDiscount_percentage());
        }
        return cart;
    }

    @Override
    public boolean isCouponApplicableOnCart(COUPON coupon, Cart cart) {
        if(coupon.getExpires_at().before(Timestamp.valueOf(LocalDateTime.now()))){
            return false;
        }
        double minPriceToApplyDiscount = (double) coupon.getConditions().get("minValueToApplyDiscount");
        return cart.getOriginalAmountOfCart() > minPriceToApplyDiscount;
    }

    private void calculateFinalPriceOfCart(Cart cart, int discount_percentage){
        if(cart.getOriginalAmountOfCart() == 0.0){
            cart.setOriginalAmountOfCart();
        }
        for(Product product : cart.getProducts()){
            product.setFinalPriceOfSingleItemAfterDiscount(product.getPriceOfSingleItem()*(1-discount_percentage*0.01));
        }
        cart.setAvailableDiscount(cart.getOriginalAmountOfCart()*discount_percentage*0.01);
        cart.setFinalAmountOfCartAfterDiscount(cart.getOriginalAmountOfCart() - cart.getAvailableDiscount());
    }
}
