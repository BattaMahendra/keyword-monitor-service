package com.mahi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for monitored site response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoredSiteResponse {

    private Long id;

    private String url;

    private String keyword; // Keep for backward compatibility on the UI for now

    private List<String> keywords;

    private String email;

    private String telegramChatId;

    private LocalDateTime lastChecked;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isActive;
}
