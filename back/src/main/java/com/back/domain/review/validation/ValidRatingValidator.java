package com.back.domain.review.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidRatingValidator implements ConstraintValidator<ValidRating, Float> {
    @Override
    public boolean isValid(Float value, ConstraintValidatorContext context) {
        if (value == null) return true;

        boolean isInRange = 0f <= value && value <= 5f;
        boolean isHalf = value * 2f % 1f == 0;

        return isInRange && isHalf;
    }
}