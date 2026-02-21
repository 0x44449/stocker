package app.sandori.stocker.ingest.news;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

@Component
public class ImageStorageClient {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageClient.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final NewsCrawlConfig config;
    private MinioClient minioClient;

    public ImageStorageClient(NewsCrawlConfig config) {
        this.config = config;
    }

    @PostConstruct
    void init() {
        if (config.getMinioEndpoint() == null || config.getMinioEndpoint().isBlank()) {
            log.warn("MinIO endpoint 미설정 - 이미지 저장 비활성화");
            return;
        }

        minioClient = MinioClient.builder()
                .endpoint(config.getMinioEndpoint())
                .credentials(config.getMinioAccessKey(), config.getMinioSecretKey())
                .build();

        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(config.getMinioBucket()).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(config.getMinioBucket()).build()
                );
                log.info("MinIO 버킷 생성: {}", config.getMinioBucket());
            }
        } catch (Exception e) {
            log.warn("MinIO 버킷 확인/생성 실패: {}", e.getMessage());
        }
    }

    public boolean isEnabled() {
        return minioClient != null;
    }

    /**
     * 이미지를 MinIO에 업로드한다.
     * @return 저장된 object key, 실패 시 null
     */
    public String upload(byte[] data, String contentType, String key) {
        if (!isEnabled()) {
            return null;
        }

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getMinioBucket())
                    .object(key)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType(contentType)
                    .build());
            return key;
        } catch (Exception e) {
            log.warn("MinIO 업로드 실패: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * object key 생성: news/{source}/{yyyyMMdd}/{url-hash-8자}.{확장자}
     */
    public String generateKey(String source, String imageUrl) {
        String date = LocalDate.now().format(DATE_FORMAT);
        String hash = hashFirst8(imageUrl);
        String ext = extractExtension(imageUrl);
        return "news/" + source + "/" + date + "/" + hash + "." + ext;
    }

    private String hashFirst8(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (Exception e) {
            return input.substring(Math.max(0, input.length() - 8));
        }
    }

    private String extractExtension(String url) {
        // 쿼리스트링 제거 후 확장자 추출
        String path = url.split("\\?")[0];
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            String ext = path.substring(lastDot + 1).toLowerCase();
            // 유효한 이미지 확장자만 허용
            if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") ||
                    ext.equals("gif") || ext.equals("webp")) {
                return ext;
            }
        }
        return "jpg";
    }
}
