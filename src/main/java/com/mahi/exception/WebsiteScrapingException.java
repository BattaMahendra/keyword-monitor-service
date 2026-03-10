package com.mahi.exception;

/**
 * Exception thrown when there's an issue with website scraping
 */
public class WebsiteScrapingException extends KeywordMonitorException {

    public WebsiteScrapingException(String url, String message) {
        super("Failed to scrape website: " + url + ". Reason: " + message, "SCRAPING_ERROR");
    }

    public WebsiteScrapingException(String url, String message, Throwable cause) {
        super("Failed to scrape website: " + url + ". Reason: " + message, "SCRAPING_ERROR", cause);
    }
}

