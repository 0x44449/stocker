package com.hanzi.stocker.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답")
public record ErrorResponse(
        @Schema(description = "에러 코드", example = "NEWS_001")
        String errorCode,

        @Schema(description = "에러 메시지 키 (클라이언트 로컬라이징용)", example = "CRAWL_FAILED")
        String message
) {
}
