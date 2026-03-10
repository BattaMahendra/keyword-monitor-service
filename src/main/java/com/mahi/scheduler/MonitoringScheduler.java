package com.mahi.scheduler;

import com.mahi.entity.MonitoredSite;
import com.mahi.service.MonitoredSiteService;
import com.mahi.service.WebsiteMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler for monitoring websites at regular intervals
 */
@Slf4j
@Component
public class MonitoringScheduler {

    @Autowired
    private MonitoredSiteService monitoredSiteService;

    @Autowired
    private WebsiteMonitorService websiteMonitorService;

    /**
     * Run website monitoring every 5 minutes
     * This method is executed at fixed rate of 300000 milliseconds (5 minutes)
     */
    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    public void checkWebsites() {
        log.info("=== Starting scheduled website monitoring ===");

        try {
            List<MonitoredSite> sitesToCheck = monitoredSiteService.getSitesToCheck();

            if (sitesToCheck.isEmpty()) {
                log.info("No active monitored sites to check");
                return;
            }

            log.info("Found {} sites to check", sitesToCheck.size());

            for (MonitoredSite site : sitesToCheck) {
                try {
                    websiteMonitorService.monitorSite(site);
                } catch (Exception e) {
                    log.error("Error monitoring site {}", site.getId(), e);
                    // Continue with next site even if one fails
                }
            }

            log.info("=== Scheduled website monitoring completed ===");

        } catch (Exception e) {
            log.error("Error in scheduled monitoring task", e);
        }
    }

    /**
     * Health check scheduler - runs every 10 minutes
     * Logs the current active monitored sites count
     */
    @Scheduled(fixedRate = 100000, initialDelay = 10000)
    public void healthCheck() {
        try {
            List<MonitoredSite> activeSites = monitoredSiteService.getSitesToCheck();
            log.info("Health check: {} active monitored sites", activeSites.size());
        } catch (Exception e) {
            log.error("Error in health check", e);
        }
    }
}

