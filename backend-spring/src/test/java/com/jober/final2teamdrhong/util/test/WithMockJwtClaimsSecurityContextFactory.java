package com.jober.final2teamdrhong.util.test;

import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockJwtClaimsSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtClaims> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtClaims annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        JwtClaims claims = JwtClaims.builder()
                .userId(annotation.userId())
                .email(annotation.email())
                .userRole(User.UserRole.valueOf(annotation.role()))
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(claims, null, null);

        context.setAuthentication(authentication);
        return context;
    }
}