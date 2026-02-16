package app.sandori.stocker.api.ingest.news.provider.hk;

import org.springframework.stereotype.Component;

@Component
public class HkUrlPolicy {

    public boolean isArticleUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        // 한국경제 기사 URL 패턴: /article/ 포함
        // 예: https://www.hankyung.com/article/2024011234567
        return url.contains("/article/");
    }
}
