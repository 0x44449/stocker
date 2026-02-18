package app.sandori.stocker.ingest.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class MarketIndexDailyRawId implements Serializable {

    private LocalDate trdDd;
    private String indexName;

    public MarketIndexDailyRawId() {}

    public MarketIndexDailyRawId(LocalDate trdDd, String indexName) {
        this.trdDd = trdDd;
        this.indexName = indexName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketIndexDailyRawId that)) return false;
        return Objects.equals(trdDd, that.trdDd) && Objects.equals(indexName, that.indexName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trdDd, indexName);
    }
}
