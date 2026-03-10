package com.mahi.exception;

/**
 * Exception thrown when a monitored site is not found
 */
public class MonitoredSiteNotFoundException extends KeywordMonitorException {

    public MonitoredSiteNotFoundException(Long id) {
        super("Monitored site with id " + id + " not found", "SITE_NOT_FOUND");
    }

    public MonitoredSiteNotFoundException(String message) {
        super(message, "SITE_NOT_FOUND");
    }
}

