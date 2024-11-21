package com.monk.commerce.coupon.service.data.repository;

import com.monk.commerce.coupon.service.data.entity.COUPON;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The interface Coupon repository Extends CrudRepository
 */
public interface CouponRepository extends CrudRepository<COUPON,String> {

    /**
     * List all coupons in db list.
     *
     * @return the list
     */
    default List<COUPON> listAllCouponsInDB(){
        Iterable<COUPON> coupons = findAll();
        return  StreamSupport
                .stream(coupons.spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Get coupon by id coupon.
     *
     * @param id the id
     * @return the coupon
     */
    default COUPON getCouponById(String id){
        Optional<COUPON> coupon = findById(id);
        return coupon.orElse(null);
    }

    /**
     * Update coupon by id coupon.
     *
     * @param id            the id
     * @param updatedCoupon the updated coupon
     * @return the coupon
     */
    default COUPON updateCouponById(String id, COUPON updatedCoupon){
        updatedCoupon.setId(id);
        return save(updatedCoupon);
    }
}
