package app.sandori.stocker.api.entities;

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
@Table(name = "news_extraction")
public class NewsExtractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "extraction_id")
    private Long extractionId;

    @Column(name = "news_id", nullable = false)
    private Long newsId;

    @Column(name = "keywords", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> keywords;

    @Column(name = "llm_response")
    private String llmResponse;

    @Column(name = "llm_model", nullable = false, length = 100)
    private String llmModel;

    @Column(name = "prompt_version", nullable = false, length = 50)
    private String promptVersion;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getExtractionId() {
        return extractionId;
    }

    public Long getNewsId() {
        return newsId;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public String getLlmResponse() {
        return llmResponse;
    }

    public String getLlmModel() {
        return llmModel;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
