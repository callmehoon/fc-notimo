package com.jober.final2teamdrhong.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityValidator implements ApplicationRunner {
    
    @Value("${jwt.secret.key:}")
    private String jwtSecretKey;
    
    @Value("${app.environment.development:true}")
    private boolean isDevelopment;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("보안 설정 검증 시작");
        
        // 1. 환경변수 기반 JWT 키 검증
        validateEnvironmentSecurity();
        
        // 2. 운영 환경 보안 설정 검증
        if (!isDevelopment) {
            validateProductionSecurity();
        }
        
        log.info("보안 설정 검증 완료");
    }
    
    /**
     * 환경변수 기반 보안 설정 검증
     */
    private void validateEnvironmentSecurity() {
        String envJwtKey = System.getenv("JWT_SECRET_KEY");
        
        if (envJwtKey == null || envJwtKey.trim().isEmpty()) {
            log.error("Critical Security Issue: JWT_SECRET_KEY environment variable is not set");
            log.error("Application cannot start without proper JWT configuration");
            
            // 개발 환경에서는 경고만, 운영 환경에서는 종료
            if (!isDevelopment) {
                log.error("Shutting down application due to missing JWT secret key");
                System.exit(1);
            } else {
                log.warn("Development mode: continuing with application.properties key");
            }
        } else {
            log.info("JWT_SECRET_KEY environment variable detected");
        }
    }
    
    /**
     * 운영 환경 보안 설정 검증
     */
    private void validateProductionSecurity() {
        log.info("운영 환경 보안 검증 실행");
        
        // H2 데이터베이스 사용 금지 (운영 환경)
        String datasourceUrl = System.getProperty("spring.datasource.url", "");
        if (datasourceUrl.contains("h2")) {
            log.error("Production Security Issue: H2 database detected in production environment");
            log.error("Please configure a production database (MySQL, PostgreSQL, etc.)");
            System.exit(1);
        }
        
        // 개발 모드 플래그 확인
        if (isDevelopment) {
            log.warn("Warning: Development mode is enabled in production environment");
            log.warn("Please set app.environment.development=false for production");
        }
        
        log.info("운영 환경 보안 검증 완료");
    }
}