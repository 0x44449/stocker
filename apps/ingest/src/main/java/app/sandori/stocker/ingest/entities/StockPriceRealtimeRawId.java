package app.sandori.stocker.ingest.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class StockPriceRealtimeRawId implements Serializable {

    private LocalDate trdDd;
    private String stockCode;
    private LocalDateTime capturedAt;

    public StockPriceRealtimeRawId() {}

    public StockPriceRealtimeRawId(LocalDate trdDd, String stockCode, LocalDateTime capturedAt) {
        this.trdDd = trdDd;
        this.stockCode = stockCode;
        this.capturedAt = capturedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockPriceRealtimeRawId that)) return false;
        return Objects.equals(trdDd, that.trdDd)
                && Objects.equals(stockCode, that.stockCode)
                && Objects.equals(capturedAt, that.capturedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trdDd, stockCode, capturedAt);
    }
}
