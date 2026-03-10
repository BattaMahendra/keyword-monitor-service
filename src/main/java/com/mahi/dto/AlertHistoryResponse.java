package com.mahi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for alert history response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertHistoryResponse {

    private Long id;

    private Long siteId;

    private Boolean keywordFound;

    private String message;

    private LocalDateTime alertTime;

    private Boolean emailSent;

    private Boolean telegramSent;
}

