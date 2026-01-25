package com.hanzi.stocker.ingest.krx.auth;

import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

class KrxLoginRequestBuilderTest {

    @Test
    void build_withValidCredentials_returnsFormData() {
        MultiValueMap<String, String> formData = new KrxLoginRequestBuilder()
                .mbrId("testuser")
                .pw("testpassword")
                .build();

        assertEquals("testuser", formData.getFirst("mbrId"));
        assertEquals("testpassword", formData.getFirst("pw"));
        assertEquals("Y", formData.getFirst("skipDup"));
        assertEquals("", formData.getFirst("mbrNm"));
        assertEquals("", formData.getFirst("telNo"));
        assertEquals("", formData.getFirst("di"));
        assertEquals("", formData.getFirst("certType"));
    }

    @Test
    void build_withoutMbrId_throwsException() {
        KrxLoginRequestBuilder builder = new KrxLoginRequestBuilder()
                .pw("testpassword");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_withoutPw_throwsException() {
        KrxLoginRequestBuilder builder = new KrxLoginRequestBuilder()
                .mbrId("testuser");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_withBlankMbrId_throwsException() {
        KrxLoginRequestBuilder builder = new KrxLoginRequestBuilder()
                .mbrId("   ")
                .pw("testpassword");

        assertThrows(IllegalStateException.class, builder::build);
    }
}
