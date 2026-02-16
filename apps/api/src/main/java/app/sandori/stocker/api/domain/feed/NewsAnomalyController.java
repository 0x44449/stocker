package app.sandori.stocker.api.domain.feed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@Tag(name = "Feed", description = "피드 카드 조회")
public class NewsAnomalyController {

    private final NewsAnomalyService service;

    public NewsAnomalyController(NewsAnomalyService service) {
        this.service = service;
    }

    @GetMapping("/news-anomalies")
    @Operation(summary = "뉴스량 이상 감지", description = "특정 종목의 뉴스 멘션이 평소 대비 급증한 종목 목록을 반환한다")
    @ApiResponse(responseCode = "200", description = "이상 감지 결과",
            content = @Content(schema = @Schema(implementation = NewsAnomalyService.AnomalyResponse.class)))
    public NewsAnomalyService.AnomalyResponse newsAnomalies() {
        return service.detect();
    }
}
