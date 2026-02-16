package app.sandori.stocker.api.entities;

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

    public LocalDate getTrdDd() {
        return trdDd;
    }

    public void setTrdDd(LocalDate trdDd) {
        this.trdDd = trdDd;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public Long getClose() {
        return close;
    }

    public void setClose(Long close) {
        this.close = close;
    }

    public Long getDiff() {
        return diff;
    }

    public void setDiff(Long diff) {
        this.diff = diff;
    }

    public BigDecimal getDiffRate() {
        return diffRate;
    }

    public void setDiffRate(BigDecimal diffRate) {
        this.diffRate = diffRate;
    }

    public Long getOpen() {
        return open;
    }

    public void setOpen(Long open) {
        this.open = open;
    }

    public Long getHigh() {
        return high;
    }

    public void setHigh(Long high) {
        this.high = high;
    }

    public Long getLow() {
        return low;
    }

    public void setLow(Long low) {
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

    public Long getListedShares() {
        return listedShares;
    }

    public void setListedShares(Long listedShares) {
        this.listedShares = listedShares;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public OffsetDateTime getIngestedAt() {
        return ingestedAt;
    }
}
