package com.mahi.scheduler;

import com.mahi.entity.MonitoredSite;
import com.mahi.service.MonitoredSiteService;
import com.mahi.service.WebsiteMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * Run website monitoring every 5 minutes
     * This method is executed at fixed rate of 300000 milliseconds (5 minutes)
     */
    @Scheduled(fixedRateString  = "${scheduler.keyword.fixedRate}", initialDelayString ="${scheduler.keyword.initialDelay}" ,timeUnit = TimeUnit.MINUTES)
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
                taskExecutor.execute(() -> {
                    try {
                        websiteMonitorService.monitorSite(site);
                    } catch (Exception e) {
                        log.error("Error monitoring site {}", site.getId(), e);
                        // Continue with next site even if one fails
                    }
                });
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
    @Scheduled(fixedRateString  = "${scheduler.health.fixedRate}", initialDelayString ="${scheduler.health.initialDelay}" ,timeUnit = TimeUnit.MINUTES)
    public void healthCheck() {
        try {
            List<MonitoredSite> activeSites = monitoredSiteService.getSitesToCheck();
            log.info("Health check: {} active monitored sites", activeSites.size());
        } catch (Exception e) {
            log.error("Error in health check", e);
        }
    }
}
