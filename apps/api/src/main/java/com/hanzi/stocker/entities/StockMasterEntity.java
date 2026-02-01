package com.hanzi.stocker.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "stock_master")
public class StockMasterEntity {

    @Id
    @Column(name = "isin_code")
    private String isinCode;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "name_kr", nullable = false)
    private String nameKr;

    @Column(name = "name_kr_short", nullable = false)
    private String nameKrShort;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "listed_date")
    private LocalDate listedDate;

    @Column(name = "market", nullable = false)
    private String market;

    @Column(name = "security_type")
    private String securityType;

    @Column(name = "department")
    private String department;

    @Column(name = "stock_type")
    private String stockType;

    @Column(name = "face_value")
    private Long faceValue;

    @Column(name = "listed_shares")
    private Long listedShares;

    @Column(name = "ingested_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime ingestedAt;

    public StockMasterEntity() {}

    public StockMasterEntity(String isinCode, String stockCode, String nameKr, String nameKrShort,
                             String nameEn, LocalDate listedDate, String market, String securityType,
                             String department, String stockType, Long faceValue, Long listedShares) {
        this.isinCode = isinCode;
        this.stockCode = stockCode;
        this.nameKr = nameKr;
        this.nameKrShort = nameKrShort;
        this.nameEn = nameEn;
        this.listedDate = listedDate;
        this.market = market;
        this.securityType = securityType;
        this.department = department;
        this.stockType = stockType;
        this.faceValue = faceValue;
        this.listedShares = listedShares;
    }
}
