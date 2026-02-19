package app.sandori.stocker.api.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Column(name = "close")
    private Long close;

    @Column(name = "diff")
    private Long diff;

    @Column(name = "diff_rate")
    private BigDecimal diffRate;

    public StockPriceDailyRawEntity() {}

    public LocalDate getTrdDd() {
        return trdDd;
    }

    public String getStockCode() {
        return stockCode;
    }

    public Long getClose() {
        return close;
    }

    public Long getDiff() {
        return diff;
    }

    public BigDecimal getDiffRate() {
        return diffRate;
    }
}
