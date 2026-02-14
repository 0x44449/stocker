package com.hanzi.stocker.entities;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class StockClusterResultId implements Serializable {

    private String stockCode;
    private OffsetDateTime clusteredAt;

    public StockClusterResultId() {}

    public StockClusterResultId(String stockCode, OffsetDateTime clusteredAt) {
        this.stockCode = stockCode;
        this.clusteredAt = clusteredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockClusterResultId that)) return false;
        return Objects.equals(stockCode, that.stockCode) && Objects.equals(clusteredAt, that.clusteredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockCode, clusteredAt);
    }
}
