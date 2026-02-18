package app.sandori.stocker.api.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "서버 상태 확인")
public class HealthController {

    @Schema(description = "헬스체크 응답")
    public record HealthResponse(
            @Schema(description = "서버 상태", example = "ok")
            String status
    ) {}

    @GetMapping("/health")
    @Operation(summary = "헬스체크", description = "서버 상태를 확인한다")
    @ApiResponse(responseCode = "200", description = "정상",
            content = @Content(schema = @Schema(implementation = HealthResponse.class)))
    public HealthResponse health() {
        return new HealthResponse("ok");
    }
}
