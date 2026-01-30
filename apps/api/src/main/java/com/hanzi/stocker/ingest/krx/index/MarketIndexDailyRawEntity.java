package com.hanzi.stocker.ingest.krx.index;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "market_index_daily_raw")
@IdClass(MarketIndexDailyRawId.class)
public class MarketIndexDailyRawEntity {

    @Id
    @Column(name = "trd_dd")
    private LocalDate trdDd;

    @Id
    @Column(name = "index_name")
    private String indexName;

    @Column(name = "close")
    private BigDecimal close;

    @Column(name = "diff")
    private BigDecimal diff;

    @Column(name = "diff_rate")
    private BigDecimal diffRate;

    @Column(name = "open")
    private BigDecimal open;

    @Column(name = "high")
    private BigDecimal high;

    @Column(name = "low")
    private BigDecimal low;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "value")
    private Long value;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "ingested_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime ingestedAt;

    public MarketIndexDailyRawEntity() {}

    public MarketIndexDailyRawEntity(LocalDate trdDd, String indexName, BigDecimal close, BigDecimal diff,
                                     BigDecimal diffRate, BigDecimal open, BigDecimal high, BigDecimal low,
                                     Long volume, Long value, Long marketCap, String source) {
        this.trdDd = trdDd;
        this.indexName = indexName;
        this.close = close;
        this.diff = diff;
        this.diffRate = diffRate;
        this.open = open;
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.value = value;
        this.marketCap = marketCap;
        this.source = source;
    }
}
