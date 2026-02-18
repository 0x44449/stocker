package app.sandori.stocker.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// JWT 검증 + allowed_user 확인. 클래스에 붙이면 전체 메서드에 적용.
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated {
}
