package com.hanzi.stocker.ingest.krx.common;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KrxCsvParser {

    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    public List<Map<String, String>> parse(byte[] csvBytes, List<String> headers) {
        var results = new ArrayList<Map<String, String>>();

        try (var reader = new InputStreamReader(new ByteArrayInputStream(csvBytes), EUC_KR);
             var parser = CSVFormat.DEFAULT.builder()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                if (record.size() != headers.size()) {
                    throw new RuntimeException("Invalid column count at line " + record.getRecordNumber());
                }

                var row = new HashMap<String, String>();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), record.get(i));
                }
                results.add(row);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse KRX CSV", e);
        }

        return results;
    }
}
