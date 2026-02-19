package app.sandori.stocker.api.entities;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_watchlist")
@IdClass(UserWatchlistId.class)
public class UserWatchlistEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "added_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime addedAt;

    public UserWatchlistEntity() {}

    public UserWatchlistEntity(String userId, String stockCode, int sortOrder) {
        this.userId = userId;
        this.stockCode = stockCode;
        this.sortOrder = sortOrder;
    }

    public String getUserId() {
        return userId;
    }

    public String getStockCode() {
        return stockCode;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public OffsetDateTime getAddedAt() {
        return addedAt;
    }
}
