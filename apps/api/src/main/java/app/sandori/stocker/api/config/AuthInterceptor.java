package app.sandori.stocker.api.config;

import app.sandori.stocker.api.domain.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 컨트롤러 메서드가 아닌 경우 (정적 리소스 등) 통과
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // @AllowPublic이 붙은 메서드는 인증 스킵
        if (handlerMethod.hasMethodAnnotation(AllowPublic.class)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "INVALID_TOKEN");
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        String uid;
        try {
            uid = authService.verifyAndGetUid(token);
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "INVALID_TOKEN");
            return false;
        }

        request.setAttribute("uid", uid);

        // @Authenticated가 붙은 메서드는 JWT 검증만 (허용 사용자 확인 안함)
        if (handlerMethod.hasMethodAnnotation(Authenticated.class)) {
            return true;
        }

        // 기본: JWT 검증 + 허용 사용자 확인
        if (!authService.isAllowedUser(uid)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "USER_NOT_ALLOWED");
            return false;
        }

        return true;
    }

    // ErrorResponse와 동일한 JSON 포맷으로 에러 응답
    private void writeError(HttpServletResponse response, int status, String errorCode, String message) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"errorCode\":\"%s\",\"message\":\"%s\"}".formatted(errorCode, message));
    }
}
