package com.monk.commerce.coupon.service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

/**
 * The type Enum type validator.
 */
public class EnumTypeValidator implements ConstraintValidator<ValidateEnum, String> {
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidateEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value!=null && Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(e -> e.toString().equalsIgnoreCase(value));
    }
}
