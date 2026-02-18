package app.sandori.stocker.ingest.news.provider.mk;

import org.springframework.stereotype.Component;

@Component
public class MkUrlPolicy {

    public boolean isArticleUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        // 매일경제 기사 URL 패턴:
        // - URL 중간에 /news/ 포함
        // - 마지막 경로 파트가 숫자
        // 예: https://www.mk.co.kr/news/economy/12345678
        if (!url.contains("/news/") || url.contains("/news/home/")) {
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
