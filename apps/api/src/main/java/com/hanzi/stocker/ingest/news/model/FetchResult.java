package com.hanzi.stocker.ingest.news.model;

public record FetchResult(
        int statusCode,
        String body,
        boolean isRateLimited
) {
    public static FetchResult success(String body) {
        return new FetchResult(200, body, false);
    }

    public static FetchResult rateLimited(int statusCode) {
        return new FetchResult(statusCode, null, true);
    }

    public static FetchResult error(int statusCode) {
        return new FetchResult(statusCode, null, false);
    }

    public boolean isSuccess() {
        return statusCode == 200 && body != null;
    }
}
