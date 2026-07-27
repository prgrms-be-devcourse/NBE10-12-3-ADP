package com.back.domain.review.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ValidTagsValidator implements ConstraintValidator<ValidTags, List<String>> {
    private List<String> value;

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null) return true;

        this.value = value;

        return isMaxSize5() && isAllTagValid() && isAllTagUnique();
    }

    private boolean isMaxSize5() {
        return value.size() <= 5;
    }

    private boolean isAllTagValid() {
        for (int i = 0; i < value.size(); i++) {
            String tag = value.get(i);
            if (1 <= tag.length() && tag.length() <= 30) continue;
            return false;
        }

        return true;
    }

    private boolean isAllTagUnique() {
        Map<String, Integer> tagCounts = new HashMap<>();

        for (int i = 0; i < value.size(); i++) {
            String tag = value.get(i);
            tagCounts.put(tag, Optional.ofNullable(tagCounts.get(tag)).orElse(0) + 1);
        }

        for (int count : tagCounts.values()) {
            if (count <= 1) continue;
            return false;
        }

        return true;
    }
}