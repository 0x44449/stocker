package com.hanzi.stocker.ingest.krx;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * KRX 크롤링 설정.
 */
@Component
@ConfigurationProperties(prefix = "krx")
public class KrxCrawlConfig {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean hasCredentials() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}
