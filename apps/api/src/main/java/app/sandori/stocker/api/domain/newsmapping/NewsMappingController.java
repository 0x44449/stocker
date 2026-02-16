package app.sandori.stocker.api.domain.newsmapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news-mappings")
@Tag(name = "News Mapping", description = "뉴스-종목 매핑 관리")
public class NewsMappingController {

    private final NewsMappingService service;

    public NewsMappingController(NewsMappingService service) {
        this.service = service;
    }

    // --- Request DTO ---

    public record SaveMappingDto(
            @NotNull List<String> stockCodes,
            String feedback
    ) {}

    // --- Endpoints ---

    @GetMapping
    @Operation(summary = "뉴스 매핑 목록 조회", description = "뉴스별 종목 매핑 상태를 조회한다")
    @ApiResponse(responseCode = "200", description = "매핑 목록",
            content = @Content(schema = @Schema(implementation = NewsMappingService.NewsMappingListResponse.class)))
    public NewsMappingService.NewsMappingListResponse list(
            @Parameter(description = "필터 (all: 전체, reviewed: 검수 완료, unreviewed: 미검수)", example = "all")
            @RequestParam(defaultValue = "all") String filter,
            @Parameter(description = "추출 필터 (all: 전체, extracted: 추출 완료, unextracted: 미추출)", example = "all")
            @RequestParam(defaultValue = "all") String extraction,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어 (제목 검색)", example = "삼성")
            @RequestParam(defaultValue = "") String search
    ) {
        return service.getList(filter, extraction, page, size, search);
    }

    @GetMapping("/{newsId}")
    @Operation(summary = "뉴스 상세 조회", description = "뉴스 상세 정보와 매핑 상태를 조회한다 (검수 화면용)")
    @ApiResponse(responseCode = "200", description = "뉴스 상세",
            content = @Content(schema = @Schema(implementation = NewsMappingService.NewsMappingDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "뉴스 없음")
    public ResponseEntity<NewsMappingService.NewsMappingDetailResponse> detail(
            @Parameter(description = "뉴스 ID", required = true, example = "123")
            @PathVariable Long newsId
    ) {
        return service.getDetail(newsId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{newsId}")
    @Operation(summary = "종목 연결 + 피드백 저장", description = "뉴스에 종목을 연결하고 피드백을 저장한다 (upsert)")
    @ApiResponse(responseCode = "204", description = "저장 완료")
    public ResponseEntity<Void> save(
            @Parameter(description = "뉴스 ID", required = true, example = "123")
            @PathVariable Long newsId,
            @Valid @RequestBody SaveMappingDto request
    ) {
        service.save(newsId, request.stockCodes(), request.feedback());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{newsId}")
    @Operation(summary = "연결 전체 해제", description = "뉴스의 종목 매핑을 삭제한다")
    @ApiResponse(responseCode = "204", description = "삭제 완료")
    @ApiResponse(responseCode = "404", description = "매핑 없음")
    public ResponseEntity<Void> delete(
            @Parameter(description = "뉴스 ID", required = true, example = "123")
            @PathVariable Long newsId
    ) {
        if (service.delete(newsId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
