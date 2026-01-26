package com.hanzi.stocker.ingest.krx.investor;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KrxInvestorFlowCsvParserTest {

    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    private final KrxInvestorFlowCsvParser parser = new KrxInvestorFlowCsvParser();

    @Test
    void parse_normalRow_parsesAllFields() {
        String csv = """
                투자자구분,거래량_매도,거래량_매수,거래량_순매수,거래대금_매도,거래대금_매수,거래대금_순매수
                "개인","1716581206","1748578011","31996805","19277216604858","19060194839120","-217021765738"
                """;

        List<InvestorFlowDailyRaw> results = parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26), "STK");

        assertEquals(1, results.size());

        InvestorFlowDailyRaw row = results.get(0);
        assertEquals("개인", row.getInvestorName());
        assertEquals("STK", row.getMarket());
        assertEquals(1716581206L, row.getSellVolume());
        assertEquals(1748578011L, row.getBuyVolume());
        assertEquals(31996805L, row.getNetVolume());
        assertEquals(19277216604858L, row.getSellValue());
        assertEquals(19060194839120L, row.getBuyValue());
        assertEquals(-217021765738L, row.getNetValue());
    }

    @Test
    void parse_multipleRows_parsesAll() {
        String csv = """
                투자자구분,거래량_매도,거래량_매수,거래량_순매수,거래대금_매도,거래대금_매수,거래대금_순매수
                "개인","100","200","100","1000","2000","1000"
                "외국인","200","100","-100","2000","1000","-1000"
                "기관","50","50","0","500","500","0"
                """;

        List<InvestorFlowDailyRaw> results = parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26), "STK");

        assertEquals(3, results.size());
        assertEquals("개인", results.get(0).getInvestorName());
        assertEquals("외국인", results.get(1).getInvestorName());
        assertEquals("기관", results.get(2).getInvestorName());
        assertEquals(-100L, results.get(1).getNetVolume());
        assertEquals(-1000L, results.get(1).getNetValue());
    }

    @Test
    void parse_invalidColumnCount_throwsExceptionAndFailsEntireFile() {
        String csv = """
                투자자구분,거래량_매도,거래량_매수,거래량_순매수,거래대금_매도,거래대금_매수,거래대금_순매수
                "개인","100","200","100","1000","2000","1000"
                "잘못된행","값1","값2"
                "외국인","200","100","-100","2000","1000","-1000"
                """;

        KrxInvestorFlowException exception = assertThrows(KrxInvestorFlowException.class, () ->
                parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26), "STK")
        );

        assertEquals(KrxInvestorFlowException.ErrorType.INVALID_COLUMN_COUNT, exception.getErrorType());
    }

    @Test
    void parse_invalidNumberFormat_skipsRowAndContinues() {
        String csv = """
                투자자구분,거래량_매도,거래량_매수,거래량_순매수,거래대금_매도,거래대금_매수,거래대금_순매수
                "개인","100","200","100","1000","2000","1000"
                "잘못된투자자","ABC","200","100","1000","2000","1000"
                "외국인","200","100","-100","2000","1000","-1000"
                """;

        List<InvestorFlowDailyRaw> results = parser.parse(csv.getBytes(EUC_KR), LocalDate.of(2026, 1, 26), "STK");

        assertEquals(2, results.size());
        assertEquals("개인", results.get(0).getInvestorName());
        assertEquals("외국인", results.get(1).getInvestorName());
    }
}
