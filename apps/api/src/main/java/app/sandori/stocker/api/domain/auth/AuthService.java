package app.sandori.stocker.api.domain.auth;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import app.sandori.stocker.api.repositories.AllowedUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final JwkProvider jwkProvider;
    private final AllowedUserRepository allowedUserRepository;

    public AuthService(@Value("${supabase.url}") String supabaseUrl,
                       AllowedUserRepository allowedUserRepository) throws Exception {
        // Supabase JWKS endpoint: {supabaseUrl}/auth/v1/.well-known/jwks.json
        var jwksUrl = URI.create(supabaseUrl + "/auth/v1/.well-known/jwks.json").toURL();
        this.jwkProvider = new JwkProviderBuilder(jwksUrl)
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
        // JWKS 키의 알고리즘에 따라 검증 방식 자동 선택
        Algorithm algorithm = switch (jwk.getAlgorithm()) {
            case "ES256" -> Algorithm.ECDSA256((ECPublicKey) jwk.getPublicKey(), null);
            case "RS256" -> Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            default -> throw new IllegalArgumentException("지원하지 않는 알고리즘: " + jwk.getAlgorithm());
        };
        DecodedJWT verified = JWT.require(algorithm).build().verify(token);
        return verified.getSubject();
    }

    public boolean isAllowedUser(String uid) {
        return allowedUserRepository.existsById(uid);
    }
}
