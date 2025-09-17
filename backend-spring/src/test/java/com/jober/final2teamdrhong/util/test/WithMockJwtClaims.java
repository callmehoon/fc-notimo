package com.jober.final2teamdrhong.util.test;

import org.springframework.security.test.context.support.WithSecurityContext;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtClaimsSecurityContextFactory.class)
public @interface WithMockJwtClaims {

    int userId() default 1;
    String email() default "test@example.com";
    String role() default "USER";
}