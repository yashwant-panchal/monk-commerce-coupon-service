package com.monk.commerce.coupon.service.service;

import com.monk.commerce.coupon.service.data.entity.COUPON;
import com.monk.commerce.coupon.service.data.entity.Cart;
import com.monk.commerce.coupon.service.data.entity.Product;
import com.monk.commerce.coupon.service.service.api.CouponExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Component("PRODUCT_WISE")
@Slf4j
public class ProductWiseCouponExecutor implements CouponExecutor<Cart> {
    @Override
    public Cart applyCoupon(@NonNull Cart cart, COUPON coupon) {
        double totalDiscountOnCart = 0.0;
        for(Product product : cart.getProducts()){
            if(Objects.equals(product.getName(), coupon.getConditions().get("product"))){
                log.info("Applying coupon : {} to product : {}",coupon.getId(),product.getName());
                product.setFinalPriceOfSingleItemAfterDiscount(product.getPriceOfSingleItem()*(1-(coupon.getDiscount_percentage()*0.01)));
                totalDiscountOnCart += (product.getPriceOfSingleItem() - product.getFinalPriceOfSingleItemAfterDiscount())*product.getQuantity();
            } else {
                product.setFinalPriceOfSingleItemAfterDiscount(product.getPriceOfSingleItem());
            }
        }
        cart.setAvailableDiscount(totalDiscountOnCart);
        cart.setFinalAmountOfCartAfterDiscount(cart.getOriginalAmountOfCart() - cart.getAvailableDiscount());
        return cart;
    }

    @Override
    public boolean isCouponApplicableOnCart(COUPON coupon, Cart cart) {
        if(coupon.getExpires_at().before(Timestamp.valueOf(LocalDateTime.now()))){
            return false;
        }
        for(Product product : cart.getProducts()){
            if(Objects.equals(product.getName(), coupon.getConditions().get("product"))){
                return true;
            }
        }
        return false;
    }
}
