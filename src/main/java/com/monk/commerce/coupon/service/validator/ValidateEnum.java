package com.monk.commerce.coupon.service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EnumTypeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateEnum {
    Class<? extends Enum<?>> enumClass();
    String message() default "Not a valid value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
