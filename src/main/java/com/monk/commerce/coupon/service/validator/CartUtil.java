package com.monk.commerce.coupon.service.validator;

import com.google.gson.Gson;
import com.monk.commerce.coupon.service.data.entity.Cart;
import com.monk.commerce.coupon.service.data.entity.Product;
import com.monk.commerce.coupon.service.exception.MonkCommerceException;

import java.util.*;

/**
 * The type Cart util.
 */
public class CartUtil {

    /**
     * The constant gson.
     */
    public static final Gson gson = new Gson();

    /**
     * Validate products.
     *
     * @param products the products
     */
    public static void validateProducts(List<Product> products){
        if(products==null || products.isEmpty()){
            throw new MonkCommerceException("Add at least one product in Cart");
        }
        Set<String> set = new HashSet<>();
        for(Product product : products){
            if(set.contains(product.getName())){
                throw new MonkCommerceException("Product with name : "+product.getName()+
                        " already exists in cart. Please increase its quantity or select a different product");
            }

            set.add(product.getName());
        }
    }

    /**
     * Create product map map.
     *
     * @param products the products
     * @return the map of Product
     */
    public static Map<String,Product> createProductMap(List<Product> products){
        Map<String,Product> productMap = new HashMap<>();
        for(Product product : products){
            productMap.put(product.getName(),product);
        }
        return productMap;
    }

    /**
     * Convert map to Product.
     *
     * @param map the map
     * @return the product
     */
    public static Product convertMapToProduct(Map<String,Object> map){
        return gson.fromJson(gson.toJson(map),Product.class);
    }
}
