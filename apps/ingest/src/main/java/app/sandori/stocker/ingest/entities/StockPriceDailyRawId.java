package app.sandori.stocker.ingest.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StockPriceDailyRawId implements Serializable {

    private LocalDate trdDd;
    private String stockCode;

    public StockPriceDailyRawId() {}

    public StockPriceDailyRawId(LocalDate trdDd, String stockCode) {
        this.trdDd = trdDd;
        this.stockCode = stockCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockPriceDailyRawId that)) return false;
        return Objects.equals(trdDd, that.trdDd) && Objects.equals(stockCode, that.stockCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trdDd, stockCode);
    }
}
