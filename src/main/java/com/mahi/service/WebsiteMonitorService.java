package com.mahi.service;

import com.mahi.entity.AlertHistory;
import com.mahi.entity.KeywordDetail;
import com.mahi.entity.MonitoredSite;
import com.mahi.exception.MonitoredSiteNotFoundException;
import com.mahi.exception.WebsiteScrapingException;
import com.mahi.notification.EmailNotificationService;
import com.mahi.notification.TelegramNotificationService;
import com.mahi.repository.AlertHistoryRepository;
import com.mahi.repository.MonitoredSiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
public class WebsiteMonitorService {

    @Autowired
    private MonitoredSiteRepository monitoredSiteRepository;

    @Autowired
    private AlertHistoryRepository alertHistoryRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private TelegramNotificationService telegramNotificationService;

    @Value("${app.monitor.connection-timeout:10000}")
    private int connectionTimeout;

    @Value("${app.monitor.read-timeout:10000}")
    private int readTimeout;

    @Value("${app.monitor.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36}")
    private String userAgent;

    public void monitorSite(MonitoredSite site) {
        log.info("Starting monitoring for site: {}", site.getUrl());
        try {
            String pageContent = fetchWebsiteContent(site.getUrl());

            for (KeywordDetail keywordDetail : site.getKeywords()) {
                int newCount = countKeywordOccurrences(pageContent, keywordDetail.getKeyword());

                if (newCount > keywordDetail.getLastCount()) {
                    log.info("Keyword '{}' count increased from {} to {} on {}",
                            keywordDetail.getKeyword(), keywordDetail.getLastCount(), newCount, site.getUrl());
                    handleKeywordCountIncreased(site, keywordDetail, newCount);
                    keywordDetail.setLastCount(newCount);
                } else {
                    log.info("Keyword '{}' count has not increased (current: {}, new: {}) on {}",
                            keywordDetail.getKeyword(), keywordDetail.getLastCount(), newCount, site.getUrl());
                }
            }

            site.setLastChecked(LocalDateTime.now());
            monitoredSiteRepository.save(site);

        } catch (WebsiteScrapingException e) {
            log.error("Error monitoring site {}: {}", site.getUrl(), e.getMessage());
            recordFailedCheck(site);
        } catch (Exception e) {
            log.error("Unexpected error while monitoring site {}", site.getUrl(), e);
            recordFailedCheck(site);
        }
    }

    private String fetchWebsiteContent(String url) {
        try {
            log.debug("Fetching content from: {}", url);
            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(connectionTimeout)
                    .get();
            String text = doc.text();
            log.debug("Successfully fetched {} characters from {}", text.length(), url);
            return text;
        } catch (IOException e) {
            throw new WebsiteScrapingException(url, "Connection timeout or network error", e);
        } catch (Exception e) {
            throw new WebsiteScrapingException(url, e.getMessage(), e);
        }
    }

    private int countKeywordOccurrences(String content, String keyword) {
        if (!StringUtils.hasText(content) || !StringUtils.hasText(keyword)) {
            return 0;
        }
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private void handleKeywordCountIncreased(MonitoredSite site, KeywordDetail keywordDetail, int newCount) {
        if (hasRecentAlert(site.getId(), keywordDetail.getKeyword())) {
            log.info("Alert for keyword '{}' already sent recently for site {}, skipping duplicate notification",
                    keywordDetail.getKeyword(), site.getId());
            return;
        }

        boolean emailSent = false;
        boolean telegramSent = false;

        if (StringUtils.hasText(site.getEmail())) {
            try {
                sendEmailNotification(site, keywordDetail, newCount);
                emailSent = true;
            } catch (Exception e) {
                log.error("Failed to send email notification for site {}", site.getId(), e);
            }
        }

        if (StringUtils.hasText(site.getTelegramChatId())) {
            try {
                sendTelegramNotification(site, keywordDetail, newCount);
                telegramSent = true;
            } catch (Exception e) {
                log.error("Failed to send Telegram notification for site {}", site.getId(), e);
            }
        }

        recordAlert(site, keywordDetail.getKeyword(), true, emailSent, telegramSent);
    }

    private void sendEmailNotification(MonitoredSite site, KeywordDetail keywordDetail, int newCount) {
        String subject = String.format("🔔 Keyword Alert: '%s' count increased to %d!", keywordDetail.getKeyword(), newCount);
        String message = buildEmailMessage(site, keywordDetail, newCount);
        emailNotificationService.sendEmail(site.getEmail(), subject, message);
        log.info("Email notification sent to: {}", site.getEmail());
    }

    private void sendTelegramNotification(MonitoredSite site, KeywordDetail keywordDetail, int newCount) {
        String message = buildTelegramMessage(site, keywordDetail, newCount);
        telegramNotificationService.sendMessage(site.getTelegramChatId(), message);
        log.info("Telegram notification sent to chat ID: {}", site.getTelegramChatId());
    }

    private String buildEmailMessage(MonitoredSite site, KeywordDetail keywordDetail, int newCount) {
        return String.format(
                "Keyword Alert Notification\n\n" +
                "Keyword: %s\n" +
                "New Count: %d (Previous: %d)\n" +
                "Website: %s\n" +
                "Time: %s\n\n" +
                "The count for keyword '%s' has increased on %s.",
                keywordDetail.getKeyword(), newCount, keywordDetail.getLastCount(), site.getUrl(), LocalDateTime.now(),
                keywordDetail.getKeyword(), site.getUrl()
        );
    }

    private String buildTelegramMessage(MonitoredSite site, KeywordDetail keywordDetail, int newCount) {
        return String.format(
                "🔔 *Keyword Count Increased!*\n\n" +
                "🔑 Keyword: `%s`\n" +
                "📈 New Count: *%d* (Previous: %d)\n" +
                "🌐 Website: %s\n" +
                "⏰ Time: %s",
                keywordDetail.getKeyword(), newCount, keywordDetail.getLastCount(), site.getUrl(), LocalDateTime.now()
        );
    }

    private boolean hasRecentAlert(Long siteId, String keyword) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        return alertHistoryRepository.hasRecentAlertForKeyword(siteId, keyword, oneHourAgo);
    }

    private void recordAlert(MonitoredSite site, String keyword, boolean keywordFound, boolean emailSent, boolean telegramSent) {
        String message = String.format("Keyword '%s' count increased on %s", keyword, site.getUrl());
        AlertHistory alert = AlertHistory.builder()
                .siteId(site.getId())
                .keywordFound(keywordFound)
                .message(message)
                .alertTime(LocalDateTime.now())
                .emailSent(emailSent)
                .telegramSent(telegramSent)
                .build();
        alertHistoryRepository.save(alert);
        log.info("Alert recorded for site {} and keyword '{}'", site.getId(), keyword);
    }

    private void recordFailedCheck(MonitoredSite site) {
        AlertHistory alert = AlertHistory.builder()
                .siteId(site.getId())
                .keywordFound(false)
                .message("Failed to check website")
                .alertTime(LocalDateTime.now())
                .emailSent(false)
                .telegramSent(false)
                .build();
        alertHistoryRepository.save(alert);
    }

    public MonitoredSite getSiteById(Long id) {
        return monitoredSiteRepository.findById(id)
                .orElseThrow(() -> new MonitoredSiteNotFoundException(id));
    }
}
