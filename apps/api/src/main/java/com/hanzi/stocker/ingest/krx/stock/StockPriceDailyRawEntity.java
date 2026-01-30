package com.hanzi.stocker.ingest.krx.stock;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "stock_price_daily_raw")
@IdClass(StockPriceDailyRawId.class)
public class StockPriceDailyRawEntity {

    @Id
    @Column(name = "trd_dd")
    private LocalDate trdDd;

    @Id
    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "market", nullable = false)
    private String market;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "close")
    private Long close;

    @Column(name = "diff")
    private Long diff;

    @Column(name = "diff_rate")
    private BigDecimal diffRate;

    @Column(name = "open")
    private Long open;

    @Column(name = "high")
    private Long high;

    @Column(name = "low")
    private Long low;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "value")
    private Long value;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "listed_shares")
    private Long listedShares;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "ingested_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime ingestedAt;

    public StockPriceDailyRawEntity() {}

    public StockPriceDailyRawEntity(LocalDate trdDd, String market, String stockCode, String stockName,
                                     Long close, Long diff, BigDecimal diffRate, Long open, Long high, Long low,
                                     Long volume, Long value, Long marketCap, Long listedShares, String source) {
        this.trdDd = trdDd;
        this.market = market;
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.close = close;
        this.diff = diff;
        this.diffRate = diffRate;
        this.open = open;
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.value = value;
        this.marketCap = marketCap;
        this.listedShares = listedShares;
        this.source = source;
    }
}
