package com.mahi.validator;

import com.mahi.dto.CreateMonitorRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class AtLeastOneContactInfoValidator implements ConstraintValidator<AtLeastOneContactInfoProvided, CreateMonitorRequest> {

    @Override
    public void initialize(AtLeastOneContactInfoProvided constraintAnnotation) {
    }

    @Override
    public boolean isValid(CreateMonitorRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let other validators handle null request
        }
        
        boolean isEmailProvided = StringUtils.hasText(request.getEmail());
        boolean isTelegramProvided = StringUtils.hasText(request.getTelegramChatId());

        return isEmailProvided || isTelegramProvided;
    }
}
