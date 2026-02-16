package com.hanzi.stocker.api.auth;

import com.hanzi.stocker.config.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record AuthMeResponse(String status, String uid) {}

    // 현재 JWT의 사용자 상태 확인 (APPROVED: 허용됨, PENDING: 대기중)
    @Authenticated
    @GetMapping("/me")
    @Operation(summary = "현재 사용자 인증 상태 확인")
    @ApiResponse(responseCode = "200", description = "인증 상태")
    public AuthMeResponse me(@RequestAttribute("uid") String uid) {
        if (authService.isAllowedUser(uid)) {
            return new AuthMeResponse("APPROVED", uid);
        }
        return new AuthMeResponse("PENDING", null);
    }
}
