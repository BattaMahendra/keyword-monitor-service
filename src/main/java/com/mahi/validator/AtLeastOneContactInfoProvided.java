package com.mahi.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AtLeastOneContactInfoValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneContactInfoProvided {
    String message() default "At least one contact method (email or Telegram chat ID) must be provided.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
