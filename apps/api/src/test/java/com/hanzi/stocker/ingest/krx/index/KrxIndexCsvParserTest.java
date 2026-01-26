package com.hanzi.stocker.ingest.krx.index;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KrxIndexCsvParserTest {

    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    private final KrxIndexCsvParser parser = new KrxIndexCsvParser();

    @Test
    void parse_normalRow_parsesAllFields() {
        String csv = """
                지수명,종가,대비,등락률,시가,고가,저가,거래량,거래대금,상장시가총액
                "코스피","2500.50","10.25","0.41","2490.00","2510.00","2485.00","500000000","15000000000000","2000000000000000"
                """;

        List<MarketIndexDailyRaw> results = parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26));

        assertEquals(1, results.size());

        MarketIndexDailyRaw row = results.get(0);
        assertEquals("코스피", row.getIndexName());
        assertEquals(new BigDecimal("2500.50"), row.getClose());
        assertEquals(new BigDecimal("10.25"), row.getDiff());
        assertEquals(new BigDecimal("0.41"), row.getDiffRate());
        assertEquals(new BigDecimal("2490.00"), row.getOpen());
        assertEquals(new BigDecimal("2510.00"), row.getHigh());
        assertEquals(new BigDecimal("2485.00"), row.getLow());
        assertEquals(500000000L, row.getVolume());
        assertEquals(15000000000000L, row.getValue());
        assertEquals(2000000000000000L, row.getMarketCap());
    }

    @Test
    void parse_rowWithNullValues_parsesWithNulls() {
        String csv = """
                지수명,종가,대비,등락률,시가,고가,저가,거래량,거래대금,상장시가총액
                "코스피 (외국주포함)",,,,,,,"612601752","30017138130375","4125555096685080"
                """;

        List<MarketIndexDailyRaw> results = parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26));

        assertEquals(1, results.size());

        MarketIndexDailyRaw row = results.get(0);
        assertEquals("코스피 (외국주포함)", row.getIndexName());
        assertNull(row.getClose());
        assertNull(row.getDiff());
        assertNull(row.getDiffRate());
        assertNull(row.getOpen());
        assertNull(row.getHigh());
        assertNull(row.getLow());
        assertEquals(612601752L, row.getVolume());
        assertEquals(30017138130375L, row.getValue());
        assertEquals(4125555096685080L, row.getMarketCap());
    }

    @Test
    void parse_multipleRows_parsesAll() {
        String csv = """
                지수명,종가,대비,등락률,시가,고가,저가,거래량,거래대금,상장시가총액
                "코스피","2500.50","10.25","0.41","2490.00","2510.00","2485.00","500000000","15000000000000","2000000000000000"
                "코스닥","800.25","-5.10","-0.63","805.00","810.00","795.00","300000000","5000000000000","500000000000000"
                """;

        List<MarketIndexDailyRaw> results = parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26));

        assertEquals(2, results.size());
        assertEquals("코스피", results.get(0).getIndexName());
        assertEquals("코스닥", results.get(1).getIndexName());
        assertEquals(new BigDecimal("-5.10"), results.get(1).getDiff());
        assertEquals(new BigDecimal("-0.63"), results.get(1).getDiffRate());
    }

    @Test
    void parse_emptyBody_returnsEmptyList() {
        String csv = "지수명,종가,대비,등락률,시가,고가,저가,거래량,거래대금,상장시가총액\n";

        List<MarketIndexDailyRaw> results = parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26));

        assertTrue(results.isEmpty());
    }

    @Test
    void parse_invalidColumnCount_throwsExceptionAndFailsEntireFile() {
        String csv = """
                지수명,종가,대비,등락률,시가,고가,저가,거래량,거래대금,상장시가총액
                "코스피","2500.50","10.25","0.41","2490.00","2510.00","2485.00","500000000","15000000000000","2000000000000000"
                "잘못된행","값1","값2"
                "코스닥","800.25","-5.10","-0.63","805.00","810.00","795.00","300000000","5000000000000","500000000000000"
                """;

        KrxIndexException exception = assertThrows(KrxIndexException.class, () ->
                parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26))
        );

        assertEquals(KrxIndexException.ErrorType.INVALID_COLUMN_COUNT, exception.getErrorType());
    }

    @Test
    void parse_invalidNumberFormat_skipsRowAndContinues() {
        String csv = """
                지수명,종가,대비,등락률,시가,고가,저가,거래량,거래대금,상장시가총액
                "코스피","2500.50","10.25","0.41","2490.00","2510.00","2485.00","500000000","15000000000000","2000000000000000"
                "잘못된지수","ABC","10.25","0.41","2490.00","2510.00","2485.00","500000000","15000000000000","2000000000000000"
                "코스닥","800.25","-5.10","-0.63","805.00","810.00","795.00","300000000","5000000000000","500000000000000"
                """;

        List<MarketIndexDailyRaw> results = parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26));

        // 잘못된 숫자 포맷 행은 skip되고 나머지 2개만 파싱됨
        assertEquals(2, results.size());
        assertEquals("코스피", results.get(0).getIndexName());
        assertEquals("코스닥", results.get(1).getIndexName());
    }
}
