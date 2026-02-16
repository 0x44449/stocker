package app.sandori.stocker.api.entities;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "stock_cluster_result")
@IdClass(StockClusterResultId.class)
public class StockClusterResultEntity {

    @Id
    @Column(name = "stock_code")
    private String stockCode;

    @Id
    @Column(name = "clustered_at")
    private OffsetDateTime clusteredAt;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "input_hash", nullable = false)
    private String inputHash;

    @Column(name = "result", nullable = false, columnDefinition = "jsonb")
    private String result;

    public StockClusterResultEntity() {}

    public String getStockCode() {
        return stockCode;
    }

    public OffsetDateTime getClusteredAt() {
        return clusteredAt;
    }

    public String getStockName() {
        return stockName;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public String getInputHash() {
        return inputHash;
    }

    public String getResult() {
        return result;
    }
}
