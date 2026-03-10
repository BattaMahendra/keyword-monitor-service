package com.mahi.repository;

import com.mahi.entity.AlertHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AlertHistory entity
 */
@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {

    /**
     * Find alert history by site ID ordered by alert time (latest first)
     */
    List<AlertHistory> findBySiteIdOrderByAlertTimeDesc(Long siteId);

    /**
     * Find the most recent alert for a specific site
     */
    Optional<AlertHistory> findFirstBySiteIdOrderByAlertTimeDesc(Long siteId);

    /**
     * Check if a keyword alert was sent within the specified time period
     */
    @Query("SELECT COUNT(a) > 0 FROM AlertHistory a WHERE a.siteId = :siteId AND a.keywordFound = true AND a.alertTime > :since")
    boolean hasRecentAlert(@Param("siteId") Long siteId, @Param("since") LocalDateTime since);

    /**
     * Check if a specific keyword alert was sent within the specified time period
     */
    @Query("SELECT COUNT(a) > 0 FROM AlertHistory a WHERE a.siteId = :siteId AND a.keyword = :keyword AND a.keywordFound = true AND a.alertTime > :since")
    boolean hasRecentAlertForKeyword(@Param("siteId") Long siteId, @Param("keyword") String keyword, @Param("since") LocalDateTime since);

    /**
     * Find all alerts for a site within a time range
     */
    @Query("SELECT a FROM AlertHistory a WHERE a.siteId = :siteId AND a.alertTime BETWEEN :startTime AND :endTime ORDER BY a.alertTime DESC")
    List<AlertHistory> findAlertsByTimeRange(@Param("siteId") Long siteId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * Count total alerts for a site
     */
    long countBySiteId(Long siteId);

    /**
     * Count alerts where keyword was found
     */
    long countBySiteIdAndKeywordFoundTrue(Long siteId);

    /**
     * Delete old alerts (older than specified date)
     */
    @Query("DELETE FROM AlertHistory a WHERE a.siteId = :siteId AND a.alertTime < :beforeDate")
    void deleteOldAlerts(@Param("siteId") Long siteId, @Param("beforeDate") LocalDateTime beforeDate);
}
