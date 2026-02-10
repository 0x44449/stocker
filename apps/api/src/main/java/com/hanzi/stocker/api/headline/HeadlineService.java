package com.hanzi.stocker.api.headline;

import com.hanzi.stocker.entities.NewsCompanyExtractionEntity;
import com.hanzi.stocker.entities.NewsRawEntity;
import com.hanzi.stocker.entities.QNewsCompanyExtractionEntity;
import com.hanzi.stocker.entities.QNewsRawEntity;
import com.hanzi.stocker.repositories.NewsCompanyExtractionResultRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 헤드라인 뉴스 조회 서비스.
 * 특정 날짜의 뉴스에서 기업명 언급 횟수를 기반으로 헤드라인 종목을 선정한다.
 */
@Service
public class HeadlineService {

    private final JPAQueryFactory queryFactory;
    private final NewsCompanyExtractionResultRepository extractionResultRepository;

    public HeadlineService(JPAQueryFactory queryFactory,
                           NewsCompanyExtractionResultRepository extractionResultRepository) {
        this.queryFactory = queryFactory;
        this.extractionResultRepository = extractionResultRepository;
    }

    // --- DTO ---

    public record ArticleDto(Long newsId, String title, LocalDateTime publishedAt) {}

    public record HeadlineDto(String companyName, int count, List<ArticleDto> articles) {}

    public record HeadlineResponse(LocalDate date, int threshold, long totalNewsCount, List<HeadlineDto> headlines) {}

    /**
     * 헤드라인 뉴스 조회.
     * 해당 날짜의 뉴스에서 기업명 언급 횟수가 threshold 이상인 종목을 선정하고 관련 기사를 반환한다.
     */
    public HeadlineResponse getHeadlines(LocalDate date, int threshold) {
        var n = QNewsRawEntity.newsRawEntity;
        var ext = QNewsCompanyExtractionEntity.newsCompanyExtractionEntity;

        // published_at이 해당 날짜인 범위: [00:00:00, 다음날 00:00:00)
        var startOfDay = date.atStartOfDay();
        var startOfNextDay = date.plusDays(1).atStartOfDay();

        // 해당 날짜의 전체 뉴스 수
        Long totalCount = queryFactory.select(n.id.count())
                .from(n)
                .where(n.publishedAt.goe(startOfDay), n.publishedAt.lt(startOfNextDay))
                .fetchOne();
        long totalNewsCount = totalCount != null ? totalCount : 0;

        // 해당 날짜 + 추출 완료(COMPLETED)인 뉴스 조회
        var newsEntities = queryFactory.selectFrom(n)
                .where(
                        n.publishedAt.goe(startOfDay),
                        n.publishedAt.lt(startOfNextDay),
                        n.id.in(
                                JPAExpressions.select(ext.newsId)
                                        .from(ext)
                                        .where(ext.status.eq("COMPLETED"))
                        )
                )
                .fetch();

        if (newsEntities.isEmpty()) {
            return new HeadlineResponse(date, threshold, totalNewsCount, List.of());
        }

        var newsMap = newsEntities.stream()
                .collect(Collectors.toMap(NewsRawEntity::getId, e -> e));
        var newsIds = newsMap.keySet().stream().toList();

        // extraction_id → news_id 매핑
        var extractions = queryFactory.selectFrom(ext)
                .where(ext.newsId.in(newsIds), ext.status.eq("COMPLETED"))
                .fetch();

        var extractionIdToNewsId = extractions.stream()
                .collect(Collectors.toMap(
                        NewsCompanyExtractionEntity::getId,
                        NewsCompanyExtractionEntity::getNewsId
                ));

        // extraction_result 조회
        var extractionIds = extractionIdToNewsId.keySet().stream().toList();
        var results = extractionResultRepository.findByExtractionIdIn(extractionIds);

        // company_name별 뉴스 ID 그룹핑 (같은 뉴스에서 같은 기업명 중복 제거)
        Map<String, Set<Long>> companyToNewsIds = new HashMap<>();
        for (var result : results) {
            var newsId = extractionIdToNewsId.get(result.getExtractionId());
            companyToNewsIds.computeIfAbsent(result.getCompanyName(), k -> new HashSet<>()).add(newsId);
        }

        // threshold 이상인 종목만 선정, count 내림차순 정렬
        var headlines = companyToNewsIds.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= threshold)
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .map(entry -> {
                    var articles = entry.getValue().stream()
                            .map(newsId -> {
                                var news = newsMap.get(newsId);
                                return new ArticleDto(news.getId(), news.getTitle(), news.getPublishedAt());
                            })
                            .toList();
                    return new HeadlineDto(entry.getKey(), entry.getValue().size(), articles);
                })
                .toList();

        return new HeadlineResponse(date, threshold, totalNewsCount, headlines);
    }
}
