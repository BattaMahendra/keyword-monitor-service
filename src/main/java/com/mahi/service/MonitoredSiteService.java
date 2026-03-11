package com.mahi.service;

import com.mahi.dto.CreateMonitorRequest;
import com.mahi.dto.MonitoredSiteResponse;
import com.mahi.entity.KeywordDetail;
import com.mahi.entity.MonitoredSite;
import com.mahi.exception.MonitoredSiteNotFoundException;
import com.mahi.repository.AlertHistoryRepository;
import com.mahi.repository.MonitoredSiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing monitored sites
 */
@Slf4j
@Service
@Transactional
public class MonitoredSiteService {

    @Autowired
    private MonitoredSiteRepository monitoredSiteRepository;

    @Autowired
    private AlertHistoryRepository alertHistoryRepository;

    /**
     * Create a new monitored site
     *
     * @param request the create monitor request
     * @return the created monitored site response
     */
    public MonitoredSiteResponse createMonitor(CreateMonitorRequest request) {
        log.info("Creating new monitored site for URL: {}", request.getUrl());

        request.trim();

        if (!isValidUrl(request.getUrl())) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        MonitoredSite site = MonitoredSite.builder()
                .url(request.getUrl())
                .email(request.getEmail())
                .telegramChatId(request.getTelegramChatId())
                .isActive(true)
                .keyword(request.getKeyword()) // Set the legacy keyword field
                .build();

        List<KeywordDetail> keywordDetails = parseKeywords(request.getKeyword(), site);
        site.setKeywords(keywordDetails);

        MonitoredSite savedSite = monitoredSiteRepository.save(site);
        log.info("Monitored site created with ID: {}", savedSite.getId());

        return convertToResponse(savedSite);
    }

    /**
     * Get all monitored sites
     *
     * @return list of monitored site responses
     */
    public List<MonitoredSiteResponse> getAllMonitors() {
        log.debug("Fetching all monitored sites");
        return monitoredSiteRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get monitored site by ID
     *
     * @param id the site ID
     * @return the monitored site response
     */
    public MonitoredSiteResponse getMonitorById(Long id) {
        log.debug("Fetching monitored site with ID: {}", id);
        MonitoredSite site = monitoredSiteRepository.findById(id)
                .orElseThrow(() -> new MonitoredSiteNotFoundException(id));

        return convertToResponse(site);
    }

    /**
     * Update a monitored site
     *
     * @param id the site ID
     * @param request the update request
     * @return the updated monitored site response
     */
    public MonitoredSiteResponse updateMonitor(Long id, CreateMonitorRequest request) {
        log.info("Updating monitored site with ID: {}", id);

        request.trim();

        MonitoredSite site = monitoredSiteRepository.findById(id)
                .orElseThrow(() -> new MonitoredSiteNotFoundException(id));

        site.setUrl(request.getUrl());
        site.setEmail(request.getEmail());
        site.setTelegramChatId(request.getTelegramChatId());
        site.setKeyword(request.getKeyword()); // Update the legacy keyword field

        // Parse new keywords from request
        List<String> newKeywordStrings = Arrays.stream(request.getKeyword().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .distinct()
                .collect(Collectors.toList());

        // Create a mutable map of existing keywords for efficient lookup and removal
        Map<String, KeywordDetail> existingKeywordsMap = site.getKeywords().stream()
                .collect(Collectors.toMap(KeywordDetail::getKeyword, kd -> kd));

        List<KeywordDetail> updatedKeywordDetails = new ArrayList<>();

        for (String newKeyword : newKeywordStrings) {
            KeywordDetail existingDetail = existingKeywordsMap.remove(newKeyword);
            if (existingDetail != null) {
                // Keyword already exists, preserve it (and its lastCount)
                updatedKeywordDetails.add(existingDetail);
            } else {
                // This is a new keyword
                updatedKeywordDetails.add(KeywordDetail.builder()
                        .keyword(newKeyword)
                        .lastCount(0)
                        .monitoredSite(site)
                        .build());
            }
        }

        // Update the site's list of keywords. Orphan removal will delete the ones left in the map.
        site.getKeywords().clear();
        site.getKeywords().addAll(updatedKeywordDetails);

        MonitoredSite updatedSite = monitoredSiteRepository.save(site);
        log.info("Monitored site with ID {} updated", id);

        return convertToResponse(updatedSite);
    }

    /**
     * Delete a monitored site (soft delete)
     *
     * @param id the site ID
     */
    public void deleteMonitor(Long id) {
        log.info("Deleting monitored site with ID: {}", id);

        MonitoredSite site = monitoredSiteRepository.findById(id)
                .orElseThrow(() -> new MonitoredSiteNotFoundException(id));

        site.setIsActive(false);
        monitoredSiteRepository.save(site);
        log.info("Monitored site with ID {} deleted", id);
    }

    /**
     * Get all sites that need to be checked
     *
     * @return list of monitored sites to check
     */
    public List<MonitoredSite> getSitesToCheck() {
        return monitoredSiteRepository.findSitesToCheck();
    }

    /**
     * Get alert count for a site
     *
     * @param siteId the site ID
     * @return the alert count
     */
    public long getAlertCount(Long siteId) {
        return alertHistoryRepository.countBySiteId(siteId);
    }

    /**
     * Get keyword found count for a site
     *
     * @param siteId the site ID
     * @return the count of times keyword was found
     */
    public long getKeywordFoundCount(Long siteId) {
        return alertHistoryRepository.countBySiteIdAndKeywordFoundTrue(siteId);
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private List<KeywordDetail> parseKeywords(String keywords, MonitoredSite site) {
        if (!StringUtils.hasText(keywords)) {
            return List.of();
        }
        return Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase) // Convert to lowercase
                .distinct() // Remove duplicates
                .map(kw -> KeywordDetail.builder().keyword(kw).lastCount(0).monitoredSite(site).build())
                .collect(Collectors.toList());
    }

    private MonitoredSiteResponse convertToResponse(MonitoredSite site) {
        List<String> keywordStrings = site.getKeywords().stream()
                .map(KeywordDetail::getKeyword)
                .collect(Collectors.toList());

        return MonitoredSiteResponse.builder()
                .id(site.getId())
                .url(site.getUrl())
                .keywords(keywordStrings)
                .keyword(String.join(", ", keywordStrings)) // For UI backward compatibility
                .email(maskEmail(site.getEmail()))
                .telegramChatId(maskTelegramChatId(site.getTelegramChatId()))
                .lastChecked(site.getLastChecked())
                .createdAt(site.getCreatedAt())
                .updatedAt(site.getUpdatedAt())
                .isActive(site.getIsActive())
                .build();
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return email; // Not enough characters to mask
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    private String maskTelegramChatId(String chatId) {
        if (!StringUtils.hasText(chatId) || chatId.length() <= 4) {
            return chatId; // Not enough characters to mask
        }
        return chatId.substring(0, 2) + "***" + chatId.substring(chatId.length() - 2);
    }
}
