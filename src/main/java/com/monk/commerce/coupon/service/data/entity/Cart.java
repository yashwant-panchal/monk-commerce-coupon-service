package com.monk.commerce.coupon.service.data.entity;

import com.monk.commerce.coupon.service.validator.CartUtil;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Cart.
 */
@Builder
@Getter
public class Cart {
    /**
     * List of Products in Cart
     */
    @Setter
    @Builder.Default private List<Product> products = new ArrayList<>();

    /**
     * Original amount of cart before applying discount
     */
    private double originalAmountOfCart;

    /**
     * Calculated discount by applying coupon
     */
    @Setter
    private double availableDiscount;

    /**
     * Final Amount of cart after applying coupon
     */
    @Setter
    private double finalAmountOfCartAfterDiscount;


    /**
     * Validate the list of products after construction
     */
    @PostConstruct
    private void validateProducts(){
        CartUtil.validateProducts(this.products);
    }

    /**
     * Get original amount of cart double.
     *
     * @return the double
     */
    public double getOriginalAmountOfCart(){
        double total = 0.0;
        for(Product product : this.getProducts()){
            total += product.getPriceOfSingleItem()*product.getQuantity();
        }
        return total;
    }

    /**
     * Set original amount of cart.
     */
    public void setOriginalAmountOfCart(){
        this.originalAmountOfCart = getOriginalAmountOfCart();
    }
}
