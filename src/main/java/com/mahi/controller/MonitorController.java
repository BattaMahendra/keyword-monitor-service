package com.mahi.controller;

import com.mahi.entity.MonitoredSite;
import com.mahi.dto.ApiResponse;
import com.mahi.dto.CreateMonitorRequest;
import com.mahi.dto.MonitoredSiteResponse;
import com.mahi.service.MonitoredSiteService;
import com.mahi.service.WebsiteMonitorService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing monitored sites
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MonitorController {

    private final MonitoredSiteService monitoredSiteService;
    private final WebsiteMonitorService websiteMonitorService;

    @Autowired
    public MonitorController(MonitoredSiteService monitoredSiteService, WebsiteMonitorService websiteMonitorService) {
        this.monitoredSiteService = monitoredSiteService;
        this.websiteMonitorService = websiteMonitorService;
    }

    /**
     * Create a new monitored site
     *
     * @param request the create monitor request
     * @return the created monitored site response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MonitoredSiteResponse>> createMonitor(@Valid @RequestBody CreateMonitorRequest request) {
        log.info("POST /api/monitor - Creating new monitored site for URL: {}", request.getUrl());

        MonitoredSiteResponse response = monitoredSiteService.createMonitor(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Monitored site created successfully"));
    }

    /**
     * Get all monitored sites
     *
     * @return list of monitored sites
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MonitoredSiteResponse>>> getAllMonitors() {
        log.info("GET /api/monitor - Fetching all monitored sites");

        List<MonitoredSiteResponse> sites = monitoredSiteService.getAllMonitors();

        return ResponseEntity.ok(ApiResponse.success(sites, "Retrieved " + sites.size() + " monitored sites"));
    }

    /**
     * Get a specific monitored site by ID
     *
     * @param id the site ID
     * @return the monitored site response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MonitoredSiteResponse>> getMonitorById(@PathVariable Long id) {
        log.info("GET /api/monitor/{} - Fetching monitored site", id);

        MonitoredSiteResponse site = monitoredSiteService.getMonitorById(id);

        return ResponseEntity.ok(ApiResponse.success(site, "Monitored site retrieved successfully"));
    }

    /**
     * Update a monitored site
     *
     * @param id the site ID
     * @param request the update request
     * @return the updated monitored site response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MonitoredSiteResponse>> updateMonitor(
            @PathVariable Long id,
            @Valid @RequestBody CreateMonitorRequest request) {
        log.info("PUT /api/monitor/{} - Updating monitored site", id);

        MonitoredSiteResponse response = monitoredSiteService.updateMonitor(id, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Monitored site updated successfully"));
    }

    /**
     * Delete a monitored site
     *
     * @param id the site ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMonitor(@PathVariable Long id) {
        log.info("DELETE /api/monitor/{} - Deleting monitored site", id);

        monitoredSiteService.deleteMonitor(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Monitored site deleted successfully"));
    }

    /**
     * Trigger manual scan for a specific monitored site
     *
     * @param id the site ID
     * @return success response
     */
    @PostMapping("/{id}/scan")
    public ResponseEntity<ApiResponse<String>> triggerManualScan(@PathVariable Long id) {
        log.info("POST /api/monitor/{}/scan - Triggering manual scan", id);

        try {
            MonitoredSite site = websiteMonitorService.getSiteById(id);
            websiteMonitorService.monitorSite(site);

            return ResponseEntity.ok(ApiResponse.success(
                    "Manual scan completed for site " + id,
                    "Website scanned successfully for keyword"
            ));
        } catch (Exception e) {
            log.error("Error during manual scan for site {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Manual scan failed: " + e.getMessage()));
        }
    }

    /**
     * Get statistics for a monitored site
     *
     * @param id the site ID
     * @return statistics response
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<Object>> getSiteStatistics(@PathVariable Long id) {
        log.info("GET /api/monitor/{}/stats - Fetching site statistics", id);

        MonitoredSiteResponse site = monitoredSiteService.getMonitorById(id);
        long totalAlerts = monitoredSiteService.getAlertCount(id);
        long keywordFoundCount = monitoredSiteService.getKeywordFoundCount(id);

        Map<String, Object> stats = new HashMap<>();
        stats.put("siteInfo", site);
        stats.put("totalAlerts", totalAlerts);
        stats.put("keywordFoundCount", keywordFoundCount);

        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
    }

    /**
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        log.debug("GET /api/monitor/health - Health check");

        return ResponseEntity.ok(ApiResponse.success(
                "Keyword Monitor Service is running",
                "Health check passed"
        ));
    }
}
