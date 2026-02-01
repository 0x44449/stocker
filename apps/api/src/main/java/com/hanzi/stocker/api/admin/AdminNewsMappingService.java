package com.hanzi.stocker.api.admin;

import com.hanzi.stocker.entities.CompanyNameMappingEntity;
import com.hanzi.stocker.entities.QCompanyNameMappingEntity;
import com.hanzi.stocker.entities.QNewsRawEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 뉴스 매핑 관리 비즈니스 로직.
 * DB에서 페이징된 news_id를 조회하고, 서비스에서 데이터를 가공한다.
 */
@Service
public class AdminNewsMappingService {

    private final JPAQueryFactory queryFactory;

    public AdminNewsMappingService(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public record NewsMappingSummaryItem(
            Long newsId,
            String title,
            List<String> extractedNames,
            int matchedCount,
            int totalCount,
            String status
    ) {}

    public record NewsMappingListResponse(
            List<NewsMappingSummaryItem> items,
            long totalCount,
            int page,
            int size
    ) {}

    public NewsMappingListResponse getList(String filter, int page, int size, String search) {
        var n = QNewsRawEntity.newsRawEntity;
        var m = QCompanyNameMappingEntity.companyNameMappingEntity;

        // news_raw 기준, company_name_mapping LEFT JOIN
        // 매핑 수 집계 (LEFT JOIN이므로 매핑 없는 뉴스는 0)
        var mappingCountExpr = m.id.count().intValue();

        // status 필터용 집계
        var verifiedCountExpr = new CaseBuilder()
                .when(m.verified.isTrue()).then(1).otherwise(0).sum();

        var where = new BooleanBuilder();
        var having = new BooleanBuilder();

        // 검색 조건
        if (search != null && !search.isBlank()) {
            var q = search.trim().toLowerCase();
            var m2 = new QCompanyNameMappingEntity("m2");
            where.and(
                    n.title.lower().contains(q)
                            .or(n.id.in(
                                    JPAExpressions.select(m2.newsId).from(m2)
                                            .where(m2.extractedName.lower().contains(q))
                            ))
            );
        }

        // status 필터
        switch (filter) {
            case "done" -> {
                // 매핑이 1개 이상이고 모두 verified
                having.and(mappingCountExpr.gt(0));
                having.and(verifiedCountExpr.eq(mappingCountExpr));
            }
            case "unmatched" -> {
                // no_mapping, unmapped, auto_pending (done이 아닌 것)
                having.and(
                        mappingCountExpr.eq(0)
                                .or(verifiedCountExpr.lt(mappingCountExpr))
                );
            }
        }

        // 1단계: 전체 건수
        long total = queryFactory.select(n.id)
                .from(n)
                .leftJoin(m).on(m.newsId.eq(n.id))
                .where(where)
                .groupBy(n.id)
                .having(having)
                .fetch().size();

        if (total == 0) {
            return new NewsMappingListResponse(List.of(), 0, page, size);
        }

        // 페이징된 news_id 조회
        var newsIds = queryFactory.select(n.id)
                .from(n)
                .leftJoin(m).on(m.newsId.eq(n.id))
                .where(where)
                .groupBy(n.id)
                .having(having)
                .orderBy(n.id.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        // 2단계: 해당 뉴스의 매핑 데이터 + 제목 조회
        var mappings = queryFactory.selectFrom(m)
                .where(m.newsId.in(newsIds))
                .fetch();

        var titleMap = queryFactory.select(n.id, n.title)
                .from(n)
                .where(n.id.in(newsIds))
                .fetch().stream()
                .collect(Collectors.toMap(row -> row.get(n.id), row -> row.get(n.title)));

        // 3단계: 서비스에서 집계
        var mappingsByNewsId = mappings.stream()
                .collect(Collectors.groupingBy(CompanyNameMappingEntity::getNewsId));

        var items = newsIds.stream().map(newsId -> {
            var group = mappingsByNewsId.getOrDefault(newsId, List.of());

            if (group.isEmpty()) {
                return new NewsMappingSummaryItem(
                        newsId, titleMap.getOrDefault(newsId, ""), List.of(), 0, 0, "no_mapping"
                );
            }

            var extractedNames = group.stream()
                    .map(CompanyNameMappingEntity::getExtractedName)
                    .distinct().toList();
            int totalCount = group.size();
            int matchedCount = (int) group.stream()
                    .filter(row -> row.getMatchedStockCode() != null).count();

            boolean allVerified = group.stream().allMatch(row -> Boolean.TRUE.equals(row.getVerified()));
            boolean anyVerified = group.stream().anyMatch(row -> Boolean.TRUE.equals(row.getVerified()));
            boolean hasAuto = group.stream().anyMatch(row -> "auto_exact".equals(row.getMatchType()));

            String status;
            if (allVerified) {
                status = "done";
            } else if (anyVerified) {
                status = "partial";
            } else if (hasAuto) {
                status = "auto_pending";
            } else {
                status = "unmapped";
            }

            return new NewsMappingSummaryItem(
                    newsId, titleMap.getOrDefault(newsId, ""), extractedNames, matchedCount, totalCount, status
            );
        }).toList();

        return new NewsMappingListResponse(items, total, page, size);
    }
}
