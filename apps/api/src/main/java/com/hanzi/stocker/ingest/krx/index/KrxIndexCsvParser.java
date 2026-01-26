package com.hanzi.stocker.ingest.krx.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * KRX CSV 파서.
 * EUC-KR 인코딩 CSV를 파싱하여 MarketIndexDailyRaw 목록으로 변환.
 */
@Component
public class KrxIndexCsvParser {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private static final Charset EUC_KR = Charset.forName("EUC-KR");
    private static final int EXPECTED_COLUMN_COUNT = 10;

    public List<MarketIndexDailyRaw> parse(byte[] csvBytes, LocalDate trdDd) {
        crawlLog.info("event=KRX_INDEX_CSV_PARSE_START trdDd={}", trdDd);

        List<MarketIndexDailyRaw> results = new ArrayList<>();
        int lineNumber = 0;
        int successCount = 0;
        int skipCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(csvBytes), EUC_KR))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                try {
                    MarketIndexDailyRaw row = parseLine(line, trdDd, lineNumber);
                    if (row != null) {
                        results.add(row);
                        successCount++;
                    }
                } catch (KrxIndexException e) {
                    if (e.getErrorType() == KrxIndexException.ErrorType.INVALID_COLUMN_COUNT) {
                        crawlLog.warn("event=KRX_INDEX_CSV_PARSE_FAILED trdDd={} line={} reason=INVALID_COLUMN_COUNT",
                                trdDd, lineNumber);
                        throw e;
                    }
                    crawlLog.warn("event=KRX_INDEX_CSV_ROW_SKIP line={} reason={}", lineNumber, e.getMessage());
                    skipCount++;
                } catch (Exception e) {
                    crawlLog.warn("event=KRX_INDEX_CSV_ROW_SKIP line={} reason={}", lineNumber, e.getMessage());
                    skipCount++;
                }
            }

        } catch (KrxIndexException e) {
            throw e;
        } catch (Exception e) {
            crawlLog.warn("event=KRX_INDEX_CSV_PARSE_FAILED trdDd={} reason={}", trdDd, e.getMessage());
            throw new KrxIndexException(
                    KrxIndexException.ErrorType.CSV_PARSE_FAILED,
                    "CSV parsing failed: " + e.getMessage(),
                    e
            );
        }

        crawlLog.info("event=KRX_INDEX_CSV_PARSE_SUCCESS trdDd={} total={} success={} skip={}",
                trdDd, lineNumber - 1, successCount, skipCount);

        return results;
    }

    private MarketIndexDailyRaw parseLine(String line, LocalDate trdDd, int lineNumber) {
        String[] columns = parseCSVLine(line);

        if (columns.length != EXPECTED_COLUMN_COUNT) {
            throw new KrxIndexException(
                    KrxIndexException.ErrorType.INVALID_COLUMN_COUNT,
                    "Expected " + EXPECTED_COLUMN_COUNT + " columns but got " + columns.length
            );
        }

        String indexName = unquote(columns[0]);
        if (indexName == null || indexName.isBlank()) {
            return null;
        }

        MarketIndexDailyRaw row = new MarketIndexDailyRaw(trdDd, indexName);
        row.setClose(parseDecimal(columns[1]));
        row.setDiff(parseDecimal(columns[2]));
        row.setDiffRate(parseDecimal(columns[3]));
        row.setOpen(parseDecimal(columns[4]));
        row.setHigh(parseDecimal(columns[5]));
        row.setLow(parseDecimal(columns[6]));
        row.setVolume(parseLong(columns[7]));
        row.setValue(parseLong(columns[8]));
        row.setMarketCap(parseLong(columns[9]));

        return row;
    }

    private String[] parseCSVLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                columns.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        columns.add(current.toString());

        return columns.toArray(new String[0]);
    }

    private String unquote(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private BigDecimal parseDecimal(String value) {
        String cleaned = cleanNumeric(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new NumberParseException("Invalid decimal value: " + value);
        }
    }

    private Long parseLong(String value) {
        String cleaned = cleanNumeric(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            throw new NumberParseException("Invalid long value: " + value);
        }
    }

    private static class NumberParseException extends RuntimeException {
        NumberParseException(String message) {
            super(message);
        }
    }

    private String cleanNumeric(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.equals("\"\"")) {
            return null;
        }
        // Remove quotes and commas
        return trimmed.replace("\"", "").replace(",", "");
    }
}
