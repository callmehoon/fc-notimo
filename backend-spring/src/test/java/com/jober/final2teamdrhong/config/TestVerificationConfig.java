package com.jober.final2teamdrhong.config;

import com.jober.final2teamdrhong.service.storage.InMemoryVerificationStorage;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 테스트 환경에서 사용할 VerificationStorage 설정
 * Fallback 로직의 복잡성을 피하고 단순한 인메모리 저장소를 사용
 */
@TestConfiguration
@Profile("test")
public class TestVerificationConfig {

    @Bean("testVerificationStorage")
    @Primary
    public VerificationStorage verificationStorage() {
        return new InMemoryVerificationStorage();
    }
}