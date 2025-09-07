package com.jober.final2teamdrhong.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("보안 검증기 기본 테스트")
class SecurityValidatorTest {

    @Test
    @DisplayName("개발환경에서 정상 실행")
    void run_developmentMode_success() throws Exception {
        // given
        SecurityValidator validator = new SecurityValidator();
        ReflectionTestUtils.setField(validator, "isDevelopment", true);
        ReflectionTestUtils.setField(validator, "jwtSecretKey", "valid-jwt-key-32-characters-long");
        
        // when & then
        assertThatCode(() -> validator.run(null))
                .doesNotThrowAnyException();
    }
}