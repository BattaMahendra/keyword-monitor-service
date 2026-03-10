package com.mahi.repository;

import com.mahi.entity.MonitoredSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MonitoredSite entity
 */
@Repository
public interface MonitoredSiteRepository extends JpaRepository<MonitoredSite, Long> {

    /**
     * Find all active monitored sites
     */
    List<MonitoredSite> findByIsActiveTrue();

    /**
     * Find a monitored site by URL
     */
    Optional<MonitoredSite> findByUrl(String url);

    /**
     * Find all monitored sites by email
     */
    List<MonitoredSite> findByEmailAndIsActiveTrue(String email);

    /**
     * Find all monitored sites by telegram chat ID
     */
    List<MonitoredSite> findByTelegramChatIdAndIsActiveTrue(String telegramChatId);

    /**
     * Count active monitored sites
     */
    long countByIsActiveTrue();

    /**
     * Find monitored sites that need to be checked (ordered by last checked time)
     */
    @Query("SELECT m FROM MonitoredSite m WHERE m.isActive = true ORDER BY COALESCE(m.lastChecked, m.createdAt) ASC")
    List<MonitoredSite> findSitesToCheck();
}
