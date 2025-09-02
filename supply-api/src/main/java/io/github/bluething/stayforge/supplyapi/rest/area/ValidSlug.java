package io.github.bluething.stayforge.supplyapi.rest.area;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.util.regex.Pattern;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSlug.SlugValidator.class)
@Documented
public @interface ValidSlug {
    String message() default "Invalid slug format. Must contain only lowercase letters, numbers, and hyphens, starting and ending with alphanumeric characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class SlugValidator implements ConstraintValidator<ValidSlug, String> {
        private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

        @Override
        public boolean isValid(String slug, ConstraintValidatorContext context) {
            if (slug == null || slug.trim().isEmpty()) {
                return false;
            }

            return SLUG_PATTERN.matcher(slug.trim()).matches();
        }
    }
}
