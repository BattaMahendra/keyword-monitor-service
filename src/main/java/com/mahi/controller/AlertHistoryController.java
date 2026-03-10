package com.mahi.controller;

import com.mahi.entity.AlertHistory;
import com.mahi.dto.AlertHistoryResponse;
import com.mahi.dto.ApiResponse;
import com.mahi.repository.AlertHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing alert history
 */
@Slf4j
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AlertHistoryController {

    @Autowired
    private AlertHistoryRepository alertHistoryRepository;

    /**
     * Get alert history for a specific monitored site
     *
     * @param siteId the site ID
     * @return list of alert history responses
     */
    @GetMapping("/site/{siteId}")
    public ResponseEntity<ApiResponse<List<AlertHistoryResponse>>> getAlertsBySite(@PathVariable Long siteId) {
        log.info("GET /api/alerts/site/{} - Fetching alert history", siteId);

        List<AlertHistoryResponse> alerts = alertHistoryRepository.findBySiteIdOrderByAlertTimeDesc(siteId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(alerts, "Retrieved " + alerts.size() + " alerts"));
    }

    /**
     * Get the most recent alert for a site
     *
     * @param siteId the site ID
     * @return the most recent alert response
     */
    @GetMapping("/site/{siteId}/latest")
    public ResponseEntity<ApiResponse<AlertHistoryResponse>> getLatestAlert(@PathVariable Long siteId) {
        log.info("GET /api/alerts/site/{}/latest - Fetching latest alert", siteId);

        return alertHistoryRepository.findFirstBySiteIdOrderByAlertTimeDesc(siteId)
                .map(alert -> ResponseEntity.ok(ApiResponse.success(convertToResponse(alert), "Latest alert retrieved")))
                .orElse(ResponseEntity.ok(ApiResponse.error("No alerts found for this site")));
    }

    /**
     * Convert AlertHistory entity to response DTO
     *
     * @param alert the alert history entity
     * @return the response DTO
     */
    private AlertHistoryResponse convertToResponse(AlertHistory alert) {
        return AlertHistoryResponse.builder()
                .id(alert.getId())
                .siteId(alert.getSiteId())
                .keywordFound(alert.getKeywordFound())
                .message(alert.getMessage())
                .alertTime(alert.getAlertTime())
                .emailSent(alert.getEmailSent())
                .telegramSent(alert.getTelegramSent())
                .build();
    }
}

