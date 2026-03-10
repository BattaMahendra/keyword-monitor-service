package com.mahi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for tracking alert history to prevent duplicate notifications
 */
@Entity
@Table(name = "alert_history", indexes = {
    @Index(name = "idx_site_id", columnList = "site_id"),
    @Index(name = "idx_alert_time", columnList = "alert_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_id", nullable = false)
    private Long siteId;

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "keyword_found", nullable = false)
    private Boolean keywordFound;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "alert_time", nullable = false)
    private LocalDateTime alertTime;

    @Column(name = "email_sent")
    private Boolean emailSent;

    @Column(name = "telegram_sent")
    private Boolean telegramSent;

    @PrePersist
    protected void onCreate() {
        alertTime = LocalDateTime.now();
    }
}
