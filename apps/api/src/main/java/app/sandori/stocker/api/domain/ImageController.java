package app.sandori.stocker.api.domain;

import app.sandori.stocker.api.config.MinioConfig;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Image", description = "이미지 프록시")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final MinioConfig minioConfig;
    private MinioClient minioClient;

    public ImageController(MinioConfig minioConfig) {
        this.minioConfig = minioConfig;
    }

    @PostConstruct
    void init() {
        if (minioConfig.getEndpoint() == null || minioConfig.getEndpoint().isBlank()) {
            log.warn("MinIO endpoint 미설정 - 이미지 프록시 비활성화");
            return;
        }

        minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
    }

    @GetMapping("/api/images/**")
    @Operation(summary = "이미지 조회", description = "MinIO에 저장된 이미지를 프록시하여 서빙한다")
    @ApiResponse(responseCode = "200", description = "이미지 바이트")
    @ApiResponse(responseCode = "404", description = "이미지 없음")
    @ApiResponse(responseCode = "503", description = "이미지 저장소 비활성화")
    public ResponseEntity<byte[]> getImage(HttpServletRequest request) {
        if (minioClient == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        // /api/images/ 이후 전체 경로를 imageKey로 사용
        String imageKey = request.getRequestURI().substring("/api/images/".length());
        if (imageKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] data = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(imageKey)
                    .build()).readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentTypeOf(imageKey));
            // 이미지는 불변이므로 장기 캐시
            headers.setCacheControl("public, max-age=31536000, immutable");

            return ResponseEntity.ok().headers(headers).body(data);
        } catch (io.minio.errors.ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** 확장자로 Content-Type 추론 */
    private MediaType contentTypeOf(String key) {
        String lower = key.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        // jpg, jpeg 및 기본값
        return MediaType.IMAGE_JPEG;
    }
}
