package com.hanzi.stocker.ingest.krx.common;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KrxCsvParser {

    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    public List<Map<String, String>> parse(byte[] csvBytes, List<String> headers) {
        List<Map<String, String>> results = new java.util.ArrayList<>();
        int lineNumber = 0;

        try (var reader = new BufferedReader(
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

                String[] columns = line.split(",", -1);
                if (columns.length != headers.size()) {
                    throw new RuntimeException("Invalid column count at line " + lineNumber);
                }

                // 각 행을 Map<String, String>으로 변환하는 로직 구현 필요
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), columns[i]);
                }
                results.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse KRX CSV", e);
        }

        return results;
    }
}
