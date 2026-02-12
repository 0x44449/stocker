package com.hanzi.stocker.ingest.news.provider.etoday;

import org.springframework.stereotype.Component;

@Component
public class EtodayUrlPolicy {

    public boolean isArticleUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        // 이투데이 기사 URL 패턴:
        // - /news/view/ 포함
        // - 마지막 경로 파트가 숫자
        // 예: https://www.etoday.co.kr/news/view/2556417
        if (!url.contains("/news/view/")) {
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
