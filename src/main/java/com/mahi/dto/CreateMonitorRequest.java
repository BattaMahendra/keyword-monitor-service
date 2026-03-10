package com.mahi.dto;

import com.mahi.validator.AtLeastOneContactInfoProvided;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * DTO for creating a new monitored site
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AtLeastOneContactInfoProvided
public class CreateMonitorRequest {

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String url;

    @NotBlank(message = "Keywords are required")
    private String keyword;

    @Email(message = "Invalid email format")
    private String email;

    private String telegramChatId;

    public void trim() {
        this.url = StringUtils.hasText(url) ? url.trim() : null;
        this.keyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        this.email = StringUtils.hasText(email) ? email.trim() : null;
        this.telegramChatId = StringUtils.hasText(telegramChatId) ? telegramChatId.trim() : null;
    }
}
