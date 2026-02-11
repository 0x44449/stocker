package com.hanzi.stocker.api.headline;

import com.hanzi.stocker.entities.NewsRawEntity;
import com.hanzi.stocker.entities.QNewsExtractionEntity;
import com.hanzi.stocker.entities.QNewsRawEntity;
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

    public HeadlineService(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
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
        var ext = QNewsExtractionEntity.newsExtractionEntity;

        var startOfDay = date.atStartOfDay();
        var startOfNextDay = date.plusDays(1).atStartOfDay();

        // 해당 날짜의 전체 뉴스 수
        Long totalCount = queryFactory.select(n.id.count())
                .from(n)
                .where(n.publishedAt.goe(startOfDay), n.publishedAt.lt(startOfNextDay))
                .fetchOne();
        long totalNewsCount = totalCount != null ? totalCount : 0;

        // 해당 날짜의 뉴스 조회
        var newsEntities = queryFactory.selectFrom(n)
                .where(n.publishedAt.goe(startOfDay), n.publishedAt.lt(startOfNextDay))
                .fetch();

        if (newsEntities.isEmpty()) {
            return new HeadlineResponse(date, threshold, totalNewsCount, List.of());
        }

        var newsMap = newsEntities.stream()
                .collect(Collectors.toMap(NewsRawEntity::getId, e -> e));
        var newsIds = newsMap.keySet().stream().toList();

        // news_extraction에서 해당 뉴스들의 추출 결과 조회 (llm_model, prompt_version 하드코딩)
        var extractions = queryFactory.selectFrom(ext)
                .where(
                        ext.newsId.in(newsIds),
                        ext.llmModel.eq("qwen2.5:7b"),
                        ext.promptVersion.eq("v1")
                )
                .fetch();

        if (extractions.isEmpty()) {
            return new HeadlineResponse(date, threshold, totalNewsCount, List.of());
        }

        // keywords를 풀어서 기업명별 뉴스 ID 그룹핑 (같은 뉴스에서 같은 기업명 중복 제거)
        Map<String, Set<Long>> companyToNewsIds = new HashMap<>();
        for (var extraction : extractions) {
            for (var keyword : extraction.getKeywords()) {
                companyToNewsIds.computeIfAbsent(keyword, k -> new HashSet<>()).add(extraction.getNewsId());
            }
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
