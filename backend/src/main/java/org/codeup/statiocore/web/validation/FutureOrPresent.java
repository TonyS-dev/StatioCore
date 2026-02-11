package org.codeup.statiocore.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure a date/time is not in the past.
 *
 * This validator checks that the annotated OffsetDateTime field is either:
 * - In the future
 * - Present (now)
 *
 * Used to prevent users from creating reservations with past dates.
 *
 * @author TonyS-dev
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureOrPresentValidator.class)
@Documented
public @interface FutureOrPresent {
    String message() default "Dates in the past are not available. Please select a future date and time.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

