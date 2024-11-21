package com.monk.commerce.coupon.service.data.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class Product {

    /**
     * Name of the product
     */
    @NotBlank
    private String name;

    /**
     * Quantity of product
     */
    @NotBlank
    private int quantity;

    /**
     * Original Price of a single item
     */
    @NotNull
    private double priceOfSingleItem;

    /**
     * Price of single item after applying discount
     */
    @Setter
    private double finalPriceOfSingleItemAfterDiscount;
}
