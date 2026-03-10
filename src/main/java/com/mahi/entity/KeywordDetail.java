package com.mahi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "keyword_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private int lastCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitored_site_id", nullable = false)
    private MonitoredSite monitoredSite;
}
