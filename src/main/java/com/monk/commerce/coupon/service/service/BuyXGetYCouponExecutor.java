package com.monk.commerce.coupon.service.service;

import com.monk.commerce.coupon.service.data.entity.COUPON;
import com.monk.commerce.coupon.service.data.entity.Cart;
import com.monk.commerce.coupon.service.data.entity.Product;
import com.monk.commerce.coupon.service.service.api.CouponExecutor;
import com.monk.commerce.coupon.service.validator.CartUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Buy x get y coupon executor.
 */
@Component("BX_GY")
@Slf4j
public class BuyXGetYCouponExecutor implements CouponExecutor<Cart> {
    @Override
    public Cart applyCoupon(Cart cart, COUPON coupon) {


        Map<String,Object> conditions = coupon.getConditions();
        Product purchasedProduct = CartUtil.convertMapToProduct((Map<String, Object>) conditions.get("buy_product"));
        Product freeProduct = CartUtil.convertMapToProduct((Map<String, Object>) conditions.get("get_product"));
        int repetition_limit = (int) conditions.get("repetition_limit");

        cart.setOriginalAmountOfCart();

        Map<String,Integer> productCountInCart = countProductNames(cart);

        double totalDiscountOnCart = 0.0;

        int minNumberOfProductTOBuy = purchasedProduct.getQuantity();
        int getNumOfFreeProduct = freeProduct.getQuantity();

        int countOfBuyPurchasedProductInCart = productCountInCart.getOrDefault(purchasedProduct.getName(),0);
        int countOfGetFreeProductInCart = productCountInCart.getOrDefault(freeProduct.getName(),0);
        int countBeforeDiscount = countOfGetFreeProductInCart;


        Map<String,Product> productMapInCart = CartUtil.createProductMap(cart.getProducts());
        double priceOfFreeItemInCart = productMapInCart.get(freeProduct.getName()).getPriceOfSingleItem();
        double sumOfAllFreeItemInCart = priceOfFreeItemInCart*countOfGetFreeProductInCart;
        while(repetition_limit>0 && countOfBuyPurchasedProductInCart >= minNumberOfProductTOBuy
                && countOfGetFreeProductInCart >= getNumOfFreeProduct ){

            repetition_limit--;
            totalDiscountOnCart += priceOfFreeItemInCart*freeProduct.getQuantity();
            countOfBuyPurchasedProductInCart -= purchasedProduct.getQuantity();
            countOfGetFreeProductInCart -= freeProduct.getQuantity();
        }

        double finalPriceOfSingleFreeItemAfterDiscount = (sumOfAllFreeItemInCart-totalDiscountOnCart)/countBeforeDiscount;

        for(Product product : cart.getProducts()){
            if(StringUtils.equals(product.getName(),freeProduct.getName())){
                product.setFinalPriceOfSingleItemAfterDiscount(finalPriceOfSingleFreeItemAfterDiscount);
            } else product.setFinalPriceOfSingleItemAfterDiscount(product.getPriceOfSingleItem());
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
        Map<String,Object> conditions = coupon.getConditions();
        Product buyProduct = CartUtil.convertMapToProduct((Map<String, Object>) conditions.get("buy_product"));
        Product getFreeProduct = CartUtil.convertMapToProduct((Map<String, Object>) conditions.get("get_product"));
        Map<String,Integer> productCountInCart = countProductNames(cart);
        if(!productCountInCart.containsKey(buyProduct.getName()) || !productCountInCart.containsKey(getFreeProduct.getName())){
            return false;
        }
        return productCountInCart.get(buyProduct.getName()) >= buyProduct.getQuantity() &&
                productCountInCart.get(getFreeProduct.getName()) >= getFreeProduct.getQuantity();
    }

    private Map<String,Integer> countProductNames(Cart cart){
        Map<String,Integer> map = new HashMap<>();
        for(Product product : cart.getProducts()){
            map.put(product.getName(),product.getQuantity());
        }
        return map;
    }

}
