package app.sandori.stocker.api.entities;

import java.io.Serializable;
import java.util.Objects;

public class UserWatchlistId implements Serializable {

    private String userId;
    private String stockCode;

    public UserWatchlistId() {}

    public UserWatchlistId(String userId, String stockCode) {
        this.userId = userId;
        this.stockCode = stockCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserWatchlistId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(stockCode, that.stockCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, stockCode);
    }
}
