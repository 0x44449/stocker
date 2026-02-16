package com.hanzi.stocker.api.auth;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hanzi.stocker.repositories.AllowedUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final JwkProvider jwkProvider;
    private final AllowedUserRepository allowedUserRepository;

    public AuthService(@Value("${supabase.url}") String supabaseUrl,
                       AllowedUserRepository allowedUserRepository) {
        // Supabase JWKS endpoint에서 공개키 자동 조회 (캐싱: 최대 10개 키, 24시간)
        this.jwkProvider = new JwkProviderBuilder(supabaseUrl)
                .cached(10, 24, TimeUnit.HOURS)
                .build();
        this.allowedUserRepository = allowedUserRepository;
    }

    /**
     * JWT 서명 검증 + 만료 확인 후 uid(sub 클레임) 추출.
     * 검증 실패 시 예외 발생.
     */
    public String verifyAndGetUid(String token) throws Exception {
        DecodedJWT decoded = JWT.decode(token);
        Jwk jwk = jwkProvider.get(decoded.getKeyId());
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
        DecodedJWT verified = JWT.require(algorithm).build().verify(token);
        return verified.getSubject();
    }

    public boolean isAllowedUser(String uid) {
        return allowedUserRepository.existsById(uid);
    }
}
