package app.sandori.stocker.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 인증 없이 접근 가능. @Authenticated 클래스 내 특정 메서드에 사용.
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowPublic {
}
