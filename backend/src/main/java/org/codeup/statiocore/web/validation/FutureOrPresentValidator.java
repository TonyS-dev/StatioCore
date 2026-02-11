package org.codeup.statiocore.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.OffsetDateTime;

/**
 * Validator implementation for @FutureOrPresent annotation.
 *
 * Validates that an OffsetDateTime is not in the past.
 * Null values are considered valid (use @NotNull separately if required).
 *
 * @author TonyS-dev
 */
public class FutureOrPresentValidator implements ConstraintValidator<FutureOrPresent, OffsetDateTime> {

    @Override
    public void initialize(FutureOrPresent constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(OffsetDateTime value, ConstraintValidatorContext context) {
        // Null values are valid - use @NotNull separately if field is required
        if (value == null) {
            return true;
        }

        // Check if the date is not before the current time
        OffsetDateTime now = OffsetDateTime.now();
        return !value.isBefore(now);
    }
}

