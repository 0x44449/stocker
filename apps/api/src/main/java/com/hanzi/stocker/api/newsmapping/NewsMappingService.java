package com.hanzi.stocker.api.newsmapping;

import com.hanzi.stocker.entities.NewsCompanyExtractionEntity;
import com.hanzi.stocker.entities.NewsCompanyExtractionResultEntity;
import com.hanzi.stocker.entities.NewsRawEntity;
import com.hanzi.stocker.entities.QNewsCompanyExtractionEntity;
import com.hanzi.stocker.entities.QNewsRawEntity;
import com.hanzi.stocker.repositories.NewsCompanyExtractionRepository;
import com.hanzi.stocker.repositories.NewsCompanyExtractionResultRepository;
import com.hanzi.stocker.repositories.NewsRawRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 뉴스-종목 매핑 관리 서비스.
 * 목록 조회 시 QueryDSL로 동적 필터링, 상세/저장/삭제는 JPA Repository 사용.
 */
@Service
public class NewsMappingService {

    private final JPAQueryFactory queryFactory;
    private final NewsRawRepository newsRawRepository;
    private final NewsCompanyExtractionRepository extractionRepository;
    private final NewsCompanyExtractionResultRepository extractionResultRepository;
    private final NewsStockManualMappingRepository manualMappingRepository;

    public NewsMappingService(
            JPAQueryFactory queryFactory,
            NewsRawRepository newsRawRepository,
            NewsCompanyExtractionRepository extractionRepository,
            NewsCompanyExtractionResultRepository extractionResultRepository,
            NewsStockManualMappingRepository manualMappingRepository) {
        this.queryFactory = queryFactory;
        this.newsRawRepository = newsRawRepository;
        this.extractionRepository = extractionRepository;
        this.extractionResultRepository = extractionResultRepository;
        this.manualMappingRepository = manualMappingRepository;
    }

    // --- DTO ---

    public record NewsDto(
            Long newsId,
            String title,
            String press,
            String source,
            LocalDateTime publishedAt,
            LocalDateTime collectedAt
    ) {}

    public record NewsDetailDto(
            Long newsId,
            String title,
            String rawText,
            String url,
            String press,
            String source,
            LocalDateTime publishedAt,
            LocalDateTime collectedAt
    ) {}

    public record MappingDto(
            List<String> stockCodes,
            boolean reviewed
    ) {}

    public record MappingDetailDto(
            List<String> stockCodes,
            String feedback
    ) {}

    public record NewsMappingListItem(
            NewsDto news,
            List<String> extractedNames,
            MappingDto mapping
    ) {}

    public record NewsMappingListResponse(
            List<NewsMappingListItem> items,
            long totalCount,
            int page,
            int size
    ) {}

    public record NewsMappingDetailResponse(
            NewsDetailDto news,
            List<String> extractedNames,
            MappingDetailDto mapping
    ) {}

    // --- 목록 조회 ---

    /**
     * 뉴스 매핑 목록 조회. filter로 검수 상태 필터링, search로 제목 검색.
     */
    public NewsMappingListResponse getList(String filter, int page, int size, String search) {
        var n = QNewsRawEntity.newsRawEntity;
        var m = QNewsStockManualMappingEntity.newsStockManualMappingEntity;

        var where = new BooleanBuilder();

        // 검색 조건: 제목에 검색어 포함
        if (search != null && !search.isBlank()) {
            where.and(n.title.lower().contains(search.trim().toLowerCase()));
        }

        // 필터 조건
        switch (filter) {
            case "reviewed" -> {
                // 검수 완료: manual_mapping row 존재
                where.and(n.id.in(
                        JPAExpressions.select(m.newsId).from(m)
                ));
            }
            case "unreviewed" -> {
                // 미검수: manual_mapping row 없음
                where.and(n.id.notIn(
                        JPAExpressions.select(m.newsId).from(m)
                ));
            }
            // "all": 조건 없음
        }

        // 전체 건수
        Long total = queryFactory.select(n.id.count())
                .from(n)
                .where(where)
                .fetchOne();

        if (total == null || total == 0) {
            return new NewsMappingListResponse(List.of(), 0, page, size);
        }

        // 페이징된 news_id 조회
        var newsIds = queryFactory.select(n.id)
                .from(n)
                .where(where)
                .orderBy(n.id.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        if (newsIds.isEmpty()) {
            return new NewsMappingListResponse(List.of(), total, page, size);
        }

        // 뉴스 정보 조회
        var newsMap = queryFactory.selectFrom(n)
                .where(n.id.in(newsIds))
                .fetch().stream()
                .collect(Collectors.toMap(NewsRawEntity::getId, e -> e));

        // extractedNames 조회: news_company_extraction -> extraction_result
        var extractedNamesMap = getExtractedNamesMap(newsIds);

        // manual_mapping 조회
        var mappingMap = queryFactory.selectFrom(m)
                .where(m.newsId.in(newsIds))
                .fetch().stream()
                .collect(Collectors.toMap(NewsStockManualMappingEntity::getNewsId, e -> e));

        // 결과 조립
        var items = newsIds.stream().map(newsId -> {
            var news = newsMap.get(newsId);
            var extractedNames = extractedNamesMap.getOrDefault(newsId, List.of());
            var mapping = mappingMap.get(newsId);

            var newsDto = new NewsDto(
                    news.getId(),
                    news.getTitle(),
                    news.getPress(),
                    news.getSource(),
                    news.getPublishedAt(),
                    news.getCollectedAt()
            );

            var mappingDto = mapping != null
                    ? new MappingDto(mapping.getStockCodes(), true)
                    : new MappingDto(List.of(), false);

            return new NewsMappingListItem(newsDto, extractedNames, mappingDto);
        }).toList();

        return new NewsMappingListResponse(items, total, page, size);
    }

    // --- 상세 조회 ---

    /**
     * 뉴스 상세 조회. 검수 화면에서 사용.
     */
    public Optional<NewsMappingDetailResponse> getDetail(Long newsId) {
        var news = newsRawRepository.findById(newsId).orElse(null);
        if (news == null) {
            return Optional.empty();
        }

        var extractedNames = getExtractedNames(newsId);
        var mapping = manualMappingRepository.findByNewsId(newsId).orElse(null);

        var newsDto = new NewsDetailDto(
                news.getId(),
                news.getTitle(),
                news.getRawText(),
                news.getUrl(),
                news.getPress(),
                news.getSource(),
                news.getPublishedAt(),
                news.getCollectedAt()
        );

        var mappingDto = mapping != null
                ? new MappingDetailDto(mapping.getStockCodes(), mapping.getFeedback())
                : new MappingDetailDto(List.of(), null);

        return Optional.of(new NewsMappingDetailResponse(newsDto, extractedNames, mappingDto));
    }

    // --- 저장 (upsert) ---

    /**
     * 뉴스-종목 매핑 저장. 기존 매핑이 있으면 갱신, 없으면 생성.
     */
    @Transactional
    public void save(Long newsId, List<String> stockCodes, String feedback) {
        var existing = manualMappingRepository.findByNewsId(newsId);

        if (existing.isPresent()) {
            var entity = existing.get();
            entity.setStockCodes(stockCodes);
            entity.setFeedback(feedback);
            manualMappingRepository.save(entity);
        } else {
            var entity = new NewsStockManualMappingEntity(newsId, stockCodes, feedback);
            manualMappingRepository.save(entity);
        }
    }

    // --- 삭제 ---

    /**
     * 뉴스-종목 매핑 삭제.
     */
    @Transactional
    public boolean delete(Long newsId) {
        if (!manualMappingRepository.existsByNewsId(newsId)) {
            return false;
        }
        manualMappingRepository.deleteByNewsId(newsId);
        return true;
    }

    // --- private helpers ---

    /**
     * 여러 뉴스의 extractedNames를 한 번에 조회.
     */
    private Map<Long, List<String>> getExtractedNamesMap(List<Long> newsIds) {
        var e = QNewsCompanyExtractionEntity.newsCompanyExtractionEntity;

        // news_id -> extraction_id 매핑
        var extractions = queryFactory.selectFrom(e)
                .where(e.newsId.in(newsIds))
                .fetch();

        if (extractions.isEmpty()) {
            return Collections.emptyMap();
        }

        var newsIdToExtractionId = extractions.stream()
                .collect(Collectors.toMap(
                        NewsCompanyExtractionEntity::getNewsId,
                        NewsCompanyExtractionEntity::getId
                ));

        var extractionIdToNewsId = extractions.stream()
                .collect(Collectors.toMap(
                        NewsCompanyExtractionEntity::getId,
                        NewsCompanyExtractionEntity::getNewsId
                ));

        // extraction_id -> company_names
        var extractionIds = newsIdToExtractionId.values().stream().toList();
        var results = extractionResultRepository.findByExtractionIdIn(extractionIds);

        return results.stream()
                .collect(Collectors.groupingBy(
                        r -> extractionIdToNewsId.get(r.getExtractionId()),
                        Collectors.mapping(NewsCompanyExtractionResultEntity::getCompanyName, Collectors.toList())
                ));
    }

    /**
     * 단일 뉴스의 extractedNames 조회.
     */
    private List<String> getExtractedNames(Long newsId) {
        var extraction = extractionRepository.findByNewsId(newsId).orElse(null);
        if (extraction == null) {
            return List.of();
        }

        return extractionResultRepository.findByExtractionId(extraction.getId())
                .stream()
                .map(NewsCompanyExtractionResultEntity::getCompanyName)
                .toList();
    }
}
