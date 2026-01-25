package com.hanzi.stocker.ingest.krx.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KrxLoginResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialize_successResponse_parsesCorrectly() throws Exception {
        String json = """
                {
                  "previousMemberYn": false,
                  "MDC_MBR_TP_CD": "P",
                  "MBR_NO": "1000005089",
                  "_error_code": "CD001",
                  "_error_message": "정상"
                }
                """;

        KrxLoginResponse response = objectMapper.readValue(json, KrxLoginResponse.class);

        assertFalse(response.isPreviousMemberYn());
        assertEquals("P", response.getMdcMbrTpCd());
        assertEquals("1000005089", response.getMbrNo());
        assertEquals("CD001", response.getErrorCode());
        assertEquals("정상", response.getErrorMessage());
        assertTrue(response.isSuccess());
    }

    @Test
    void isSuccess_withCD001_returnsTrue() throws Exception {
        String json = """
                {
                  "_error_code": "CD001",
                  "_error_message": "정상"
                }
                """;

        KrxLoginResponse response = objectMapper.readValue(json, KrxLoginResponse.class);

        assertTrue(response.isSuccess());
    }

    @Test
    void isSuccess_withOtherCode_returnsFalse() throws Exception {
        String json = """
                {
                  "_error_code": "CD002",
                  "_error_message": "비밀번호가 틀렸습니다"
                }
                """;

        KrxLoginResponse response = objectMapper.readValue(json, KrxLoginResponse.class);

        assertFalse(response.isSuccess());
    }
}
