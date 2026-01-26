package com.hanzi.stocker.ingest.krx.index;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * market_index_daily_raw 복합 키.
 */
public class MarketIndexDailyRawId implements Serializable {

    private LocalDate trdDd;
    private String indexName;

    public MarketIndexDailyRawId() {
    }

    public MarketIndexDailyRawId(LocalDate trdDd, String indexName) {
        this.trdDd = trdDd;
        this.indexName = indexName;
    }

    public LocalDate getTrdDd() {
        return trdDd;
    }

    public String getIndexName() {
        return indexName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketIndexDailyRawId that = (MarketIndexDailyRawId) o;
        return Objects.equals(trdDd, that.trdDd) && Objects.equals(indexName, that.indexName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trdDd, indexName);
    }
}
