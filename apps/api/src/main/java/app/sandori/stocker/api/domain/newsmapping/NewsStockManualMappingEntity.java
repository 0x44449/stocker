package app.sandori.stocker.api.domain.newsmapping;

import app.sandori.stocker.api.entities.StringListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "news_stock_manual_mapping")
public class NewsStockManualMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "news_id", nullable = false, unique = true)
    private Long newsId;

    @Column(name = "stock_codes", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> stockCodes;

    @Column(name = "feedback")
    private String feedback;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public NewsStockManualMappingEntity() {
    }

    public NewsStockManualMappingEntity(Long newsId, List<String> stockCodes, String feedback) {
        this.newsId = newsId;
        this.stockCodes = stockCodes;
        this.feedback = feedback;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getNewsId() {
        return newsId;
    }

    public List<String> getStockCodes() {
        return stockCodes;
    }

    public void setStockCodes(List<String> stockCodes) {
        this.stockCodes = stockCodes;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
