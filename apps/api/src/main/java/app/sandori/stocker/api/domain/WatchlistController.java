package app.sandori.stocker.api.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watchlist")
@Tag(name = "Watchlist", description = "관심종목 관리")
public class WatchlistController {

    @GetMapping
    @Operation(summary = "관심종목 목록 조회", description = "등록된 관심종목 목록을 조회한다")
    @ApiResponse(responseCode = "200", description = "관심종목 목록")
    public void getWatchlist() {
        // TODO: 관심종목 목록 조회
    }

    @PostMapping("/{code}")
    @Operation(summary = "관심종목 추가", description = "종목을 관심종목에 추가한다")
    @ApiResponse(responseCode = "200", description = "추가 성공")
    @ApiResponse(responseCode = "404", description = "종목 없음")
    @ApiResponse(responseCode = "409", description = "이미 등록됨")
    public void addToWatchlist(
            @Parameter(description = "종목코드", required = true, example = "005930")
            @PathVariable String code) {
        // TODO: 관심종목 추가
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "관심종목 삭제", description = "종목을 관심종목에서 삭제한다")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "등록되지 않은 종목")
    public void removeFromWatchlist(
            @Parameter(description = "종목코드", required = true, example = "005930")
            @PathVariable String code) {
        // TODO: 관심종목 삭제
    }
}
