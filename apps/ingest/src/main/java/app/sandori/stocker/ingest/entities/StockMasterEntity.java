package app.sandori.stocker.ingest.entities;

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

    public String getIsinCode() {
        return isinCode;
    }

    public void setIsinCode(String isinCode) {
        this.isinCode = isinCode;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getNameKr() {
        return nameKr;
    }

    public void setNameKr(String nameKr) {
        this.nameKr = nameKr;
    }

    public String getNameKrShort() {
        return nameKrShort;
    }

    public void setNameKrShort(String nameKrShort) {
        this.nameKrShort = nameKrShort;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public LocalDate getListedDate() {
        return listedDate;
    }

    public void setListedDate(LocalDate listedDate) {
        this.listedDate = listedDate;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStockType() {
        return stockType;
    }

    public void setStockType(String stockType) {
        this.stockType = stockType;
    }

    public Long getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(Long faceValue) {
        this.faceValue = faceValue;
    }

    public Long getListedShares() {
        return listedShares;
    }

    public void setListedShares(Long listedShares) {
        this.listedShares = listedShares;
    }

    public OffsetDateTime getIngestedAt() {
        return ingestedAt;
    }
}
