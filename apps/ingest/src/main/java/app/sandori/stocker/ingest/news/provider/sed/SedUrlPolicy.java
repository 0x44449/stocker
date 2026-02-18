package app.sandori.stocker.ingest.news.provider.sed;

import org.springframework.stereotype.Component;

@Component
public class SedUrlPolicy {

    public boolean isArticleUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        // 서울경제 기사 URL 패턴:
        // - /article/ 포함
        // - 마지막 경로 파트가 숫자
        // 예: https://www.sedaily.com/article/20008365
        if (!url.contains("/article/")) {
            return false;
        }

        int lastSlash = url.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == url.length() - 1) {
            return false;
        }

        String lastPart = url.substring(lastSlash + 1);
        for (int i = 0; i < lastPart.length(); i++) {
            if (!Character.isDigit(lastPart.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
