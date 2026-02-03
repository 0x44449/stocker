package com.hanzi.stocker.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "news_company_extraction_result")
public class NewsCompanyExtractionResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "extraction_id", nullable = false)
    private Long extractionId;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    public Long getId() {
        return id;
    }

    public Long getExtractionId() {
        return extractionId;
    }

    public String getCompanyName() {
        return companyName;
    }
}
