package com.hanzi.stocker.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_name_mapping")
public class CompanyNameMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "news_id", nullable = false)
    private Long newsId;

    @Column(name = "extracted_name", length = 200)
    private String extractedName;

    @Column(name = "matched_stock_code", length = 20)
    private String matchedStockCode;

    @Column(name = "match_type", nullable = false, length = 20)
    private String matchType;

    @Column(nullable = false)
    private Boolean verified;

    private String feedback;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public Long getNewsId() { return newsId; }
    public String getExtractedName() { return extractedName; }
    public String getMatchedStockCode() { return matchedStockCode; }
    public String getMatchType() { return matchType; }
    public Boolean getVerified() { return verified; }
    public String getFeedback() { return feedback; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
