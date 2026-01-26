package com.hanzi.stocker.ingest.krx.index;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * KRX 일별 전체 지수 시세 원본(raw) 엔티티.
 */
@Entity
@Table(name = "market_index_daily_raw")
@IdClass(MarketIndexDailyRawId.class)
public class MarketIndexDailyRaw {

    @Id
    @Column(name = "trd_dd", nullable = false)
    private LocalDate trdDd;

    @Id
    @Column(name = "index_name", nullable = false)
    private String indexName;

    @Column(name = "close", precision = 18, scale = 2)
    private BigDecimal close;

    @Column(name = "diff", precision = 18, scale = 2)
    private BigDecimal diff;

    @Column(name = "diff_rate", precision = 18, scale = 2)
    private BigDecimal diffRate;

    @Column(name = "open", precision = 18, scale = 2)
    private BigDecimal open;

    @Column(name = "high", precision = 18, scale = 2)
    private BigDecimal high;

    @Column(name = "low", precision = 18, scale = 2)
    private BigDecimal low;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "value")
    private Long value;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "source", nullable = false)
    private String source = "KRX";

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    protected MarketIndexDailyRaw() {
    }

    public MarketIndexDailyRaw(LocalDate trdDd, String indexName) {
        this.trdDd = trdDd;
        this.indexName = indexName;
        this.ingestedAt = Instant.now();
    }

    public LocalDate getTrdDd() {
        return trdDd;
    }

    public String getIndexName() {
        return indexName;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getDiff() {
        return diff;
    }

    public void setDiff(BigDecimal diff) {
        this.diff = diff;
    }

    public BigDecimal getDiffRate() {
        return diffRate;
    }

    public void setDiffRate(BigDecimal diffRate) {
        this.diffRate = diffRate;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }

    public String getSource() {
        return source;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(Instant ingestedAt) {
        this.ingestedAt = ingestedAt;
    }
}
