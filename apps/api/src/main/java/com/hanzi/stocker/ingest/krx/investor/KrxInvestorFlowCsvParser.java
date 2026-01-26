package com.hanzi.stocker.ingest.krx.investor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * KRX 투자자별 거래실적 CSV 파서.
 */
@Component
public class KrxInvestorFlowCsvParser {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private static final Charset EUC_KR = Charset.forName("EUC-KR");
    private static final int EXPECTED_COLUMN_COUNT = 7;

    public List<InvestorFlowDailyRaw> parse(byte[] csvBytes, LocalDate trdDd, String market) {
        crawlLog.info("event=KRX_INVESTOR_CSV_PARSE_START trdDd={} market={}", trdDd, market);

        List<InvestorFlowDailyRaw> results = new ArrayList<>();
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
                    InvestorFlowDailyRaw row = parseLine(line, trdDd, market, lineNumber);
                    if (row != null) {
                        results.add(row);
                        successCount++;
                    }
                } catch (KrxInvestorFlowException e) {
                    if (e.getErrorType() == KrxInvestorFlowException.ErrorType.INVALID_COLUMN_COUNT) {
                        crawlLog.warn("event=KRX_INVESTOR_CSV_PARSE_FAILED trdDd={} line={} reason=INVALID_COLUMN_COUNT",
                                trdDd, lineNumber);
                        throw e;
                    }
                    crawlLog.warn("event=KRX_INVESTOR_CSV_ROW_SKIP line={} reason={}", lineNumber, e.getMessage());
                    skipCount++;
                } catch (Exception e) {
                    crawlLog.warn("event=KRX_INVESTOR_CSV_ROW_SKIP line={} reason={}", lineNumber, e.getMessage());
                    skipCount++;
                }
            }

        } catch (KrxInvestorFlowException e) {
            throw e;
        } catch (Exception e) {
            crawlLog.warn("event=KRX_INVESTOR_CSV_PARSE_FAILED trdDd={} reason={}", trdDd, e.getMessage());
            throw new KrxInvestorFlowException(
                    KrxInvestorFlowException.ErrorType.CSV_PARSE_FAILED,
                    "CSV parsing failed: " + e.getMessage(),
                    e
            );
        }

        crawlLog.info("event=KRX_INVESTOR_CSV_PARSE_SUCCESS trdDd={} market={} total={} success={} skip={}",
                trdDd, market, lineNumber - 1, successCount, skipCount);

        return results;
    }

    private InvestorFlowDailyRaw parseLine(String line, LocalDate trdDd, String market, int lineNumber) {
        String[] columns = parseCSVLine(line);

        if (columns.length != EXPECTED_COLUMN_COUNT) {
            throw new KrxInvestorFlowException(
                    KrxInvestorFlowException.ErrorType.INVALID_COLUMN_COUNT,
                    "Expected " + EXPECTED_COLUMN_COUNT + " columns but got " + columns.length
            );
        }

        String investorName = unquote(columns[0]);
        if (investorName == null || investorName.isBlank()) {
            return null;
        }

        InvestorFlowDailyRaw row = new InvestorFlowDailyRaw(trdDd, market, investorName);
        row.setSellVolume(parseLong(columns[1]));
        row.setBuyVolume(parseLong(columns[2]));
        row.setNetVolume(parseLong(columns[3]));
        row.setSellValue(parseLong(columns[4]));
        row.setBuyValue(parseLong(columns[5]));
        row.setNetValue(parseLong(columns[6]));

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

    private String cleanNumeric(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.equals("\"\"")) {
            return null;
        }
        return trimmed.replace("\"", "").replace(",", "");
    }

    private static class NumberParseException extends RuntimeException {
        NumberParseException(String message) {
            super(message);
        }
    }
}
