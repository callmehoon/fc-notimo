package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

/**
 * BlacklistService 테스트
 */
@ExtendWith(MockitoExtension.class)
class BlacklistServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private BlacklistService blacklistService;

    // 테스트 상수
    private static final String TEST_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token_payload";
    private static final String TEST_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token_payload";
    private static final String TEST_JTI = "12345678-1234-1234-1234-123456789012";
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(jwtConfig.getJtiFromToken(anyString())).thenReturn(TEST_JTI);
    }

    @Test
    @DisplayName("addAccessTokenToBlacklist: Access Token 블랙리스트 추가 성공 (15분 TTL)")
    void addAccessTokenToBlacklist_Success() {
        // when
        blacklistService.addAccessTokenToBlacklist(TEST_ACCESS_TOKEN);

        // then
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;
        verify(valueOperations).set(expectedKey, "blacklisted", Duration.ofSeconds(15 * 60));
        verify(jwtConfig).getJtiFromToken(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("addAccessTokenToBlacklist: JTI 추출 실패 시 RuntimeException 발생")
    void addAccessTokenToBlacklist_JtiExtractionFails_ThrowsException() {
        // given
        given(jwtConfig.getJtiFromToken(TEST_ACCESS_TOKEN)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> blacklistService.addAccessTokenToBlacklist(TEST_ACCESS_TOKEN))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to add token to blacklist")
                .hasCauseInstanceOf(IllegalArgumentException.class);
        verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    @DisplayName("addAccessTokenToBlacklist: Redis 오류 시 RuntimeException 발생")
    void addAccessTokenToBlacklist_RedisOperationFails_ThrowsException() {
        // given
        doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations).set(anyString(), any(), any(Duration.class));

        // when & then
        assertThatThrownBy(() -> blacklistService.addAccessTokenToBlacklist(TEST_ACCESS_TOKEN))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to add token to blacklist");
    }

    @Test
    @DisplayName("addRefreshTokenToBlacklist: Refresh Token 블랙리스트 추가 성공 (7일 TTL)")
    void addRefreshTokenToBlacklist_Success() {
        // when
        blacklistService.addRefreshTokenToBlacklist(TEST_REFRESH_TOKEN);

        // then
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;
        verify(valueOperations).set(expectedKey, "blacklisted", Duration.ofSeconds(7 * 24 * 60 * 60));
        verify(jwtConfig).getJtiFromToken(TEST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("addRefreshTokenToBlacklist: JTI 추출 실패 시 RuntimeException 발생")
    void addRefreshTokenToBlacklist_JtiExtractionFails_ThrowsException() {
        // given
        given(jwtConfig.getJtiFromToken(TEST_REFRESH_TOKEN)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> blacklistService.addRefreshTokenToBlacklist(TEST_REFRESH_TOKEN))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to add token to blacklist")
                .hasCauseInstanceOf(IllegalArgumentException.class);
        verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    @DisplayName("addTokenToBlacklist: 커스텀 TTL 설정 성공")
    void addTokenToBlacklist_WithCustomTtl_Success() {
        // given
        long customTtlSeconds = 3600; // 1시간

        // when
        blacklistService.addTokenToBlacklist(TEST_ACCESS_TOKEN, customTtlSeconds);

        // then
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;
        verify(valueOperations).set(expectedKey, "blacklisted", Duration.ofSeconds(customTtlSeconds));
        verify(jwtConfig).getJtiFromToken(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("addTokenToBlacklist: JwtConfig에서 예외 발생 시 RuntimeException 처리")
    void addTokenToBlacklist_JwtConfigException_HandlesException() {
        // given
        given(jwtConfig.getJtiFromToken(TEST_ACCESS_TOKEN))
                .willThrow(new RuntimeException("JWT parsing failed"));

        // when & then
        assertThatThrownBy(() -> blacklistService.addTokenToBlacklist(TEST_ACCESS_TOKEN, 900))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to add token to blacklist");
    }

    @Test
    @DisplayName("isTokenBlacklisted: 토큰이 블랙리스트에 존재함")
    void isTokenBlacklisted_TokenIsBlacklisted_ReturnsTrue() {
        // given
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;
        given(redisTemplate.hasKey(expectedKey)).willReturn(true);

        // when
        boolean result = blacklistService.isTokenBlacklisted(TEST_ACCESS_TOKEN);

        // then
        assertThat(result).isTrue();
        verify(redisTemplate).hasKey(expectedKey);
        verify(jwtConfig).getJtiFromToken(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("isTokenBlacklisted: 토큰이 블랙리스트에 존재하지 않음")
    void isTokenBlacklisted_TokenIsNotBlacklisted_ReturnsFalse() {
        // given
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;
        given(redisTemplate.hasKey(expectedKey)).willReturn(false);

        // when
        boolean result = blacklistService.isTokenBlacklisted(TEST_ACCESS_TOKEN);

        // then
        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(expectedKey);
        verify(jwtConfig).getJtiFromToken(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("isTokenBlacklisted: JTI 추출 실패 시 보안상 true 반환")
    void isTokenBlacklisted_JtiExtractionFails_ReturnsTrueForSecurity() {
        // given
        given(jwtConfig.getJtiFromToken(TEST_ACCESS_TOKEN)).willReturn(null);

        // when
        boolean result = blacklistService.isTokenBlacklisted(TEST_ACCESS_TOKEN);

        // then
        assertThat(result).isTrue();
        verify(redisTemplate, never()).hasKey(anyString());
    }

    @Test
    @DisplayName("isTokenBlacklisted: Redis 오류 시 보안상 true 반환 (Fail-Safe)")
    void isTokenBlacklisted_RedisOperationFails_ReturnsTrueForFailSafe() {
        // given
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;
        given(redisTemplate.hasKey(expectedKey)).willThrow(new RuntimeException("Redis connection failed"));

        // when
        boolean result = blacklistService.isTokenBlacklisted(TEST_ACCESS_TOKEN);

        // then
        assertThat(result).isTrue(); // Fail-Safe 메커니즘
        verify(jwtConfig).getJtiFromToken(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("isTokenBlacklisted: Redis hasKey 결과가 null인 경우 false 반환")
    void isTokenBlacklisted_NullRedisResponse_ReturnsFalse() {
        // given
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;
        given(redisTemplate.hasKey(expectedKey)).willReturn(null);

        // when
        boolean result = blacklistService.isTokenBlacklisted(TEST_ACCESS_TOKEN);

        // then
        assertThat(result).isFalse();
        verify(jwtConfig).getJtiFromToken(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("removeTokenFromBlacklist: 토큰 블랙리스트 제거 성공")
    void removeTokenFromBlacklist_Success() {
        // when
        blacklistService.removeTokenFromBlacklist(TEST_ACCESS_TOKEN);

        // then
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;
        verify(redisTemplate).delete(expectedKey);
        verify(jwtConfig).getJtiFromToken(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("removeTokenFromBlacklist: JTI 추출 실패 시 조용히 처리")
    void removeTokenFromBlacklist_JtiExtractionFails_HandlesSilently() {
        // given
        given(jwtConfig.getJtiFromToken(TEST_ACCESS_TOKEN)).willReturn(null);

        // when
        blacklistService.removeTokenFromBlacklist(TEST_ACCESS_TOKEN);

        // then
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("removeTokenFromBlacklist: Redis 오류 시 예외가 전파되지 않음")
    void removeTokenFromBlacklist_RedisError_DoesNotPropagateException() {
        // given
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;
        doThrow(new RuntimeException("Redis operation failed")).when(redisTemplate).delete(expectedKey);

        // when & then - 예외가 발생하지 않아야 함
        assertThatCode(() -> blacklistService.removeTokenFromBlacklist(TEST_ACCESS_TOKEN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("removeAllUserTokensFromBlacklist: 사용자 모든 토큰 블랙리스트 제거 (현재는 로그만 출력)")
    void removeAllUserTokensFromBlacklist_LogsOnly() {
        // given
        Long userId = 1L;

        // when
        blacklistService.removeAllUserTokensFromBlacklist(userId);

        // then - 현재 구현에서는 Redis delete 호출 없음
        verify(redisTemplate, never()).delete(anyString());
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("removeAllUserTokensFromBlacklist: null userId 처리")
    void removeAllUserTokensFromBlacklist_NullUserId_HandlesSilently() {
        // when
        blacklistService.removeAllUserTokensFromBlacklist(null);

        // then - 현재 구현에서는 Redis delete 호출 없음
        verify(redisTemplate, never()).delete(anyString());
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("통합 시나리오: 토큰 라이프사이클 - 추가, 확인, 제거")
    void integrationScenario_CompleteTokenLifecycle() {
        // given
        String expectedKey = BLACKLIST_KEY_PREFIX + TEST_JTI;

        // 1. 토큰을 블랙리스트에 추가
        blacklistService.addAccessTokenToBlacklist(TEST_ACCESS_TOKEN);
        verify(valueOperations).set(expectedKey, "blacklisted", Duration.ofSeconds(15 * 60));

        // 2. 토큰이 블랙리스트에 있는지 확인
        given(redisTemplate.hasKey(expectedKey)).willReturn(true);
        boolean isBlacklisted = blacklistService.isTokenBlacklisted(TEST_ACCESS_TOKEN);
        assertThat(isBlacklisted).isTrue();

        // 3. 토큰을 블랙리스트에서 제거
        blacklistService.removeTokenFromBlacklist(TEST_ACCESS_TOKEN);
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    @DisplayName("통합 시나리오: Access Token과 Refresh Token의 서로 다른 TTL 확인")
    void integrationScenario_DifferentTtlForAccessAndRefreshTokens() {
        // given
        String accessJti = "access-token-jti";
        String refreshJti = "refresh-token-jti";

        given(jwtConfig.getJtiFromToken(TEST_ACCESS_TOKEN)).willReturn(accessJti);
        given(jwtConfig.getJtiFromToken(TEST_REFRESH_TOKEN)).willReturn(refreshJti);

        // when
        blacklistService.addAccessTokenToBlacklist(TEST_ACCESS_TOKEN);
        blacklistService.addRefreshTokenToBlacklist(TEST_REFRESH_TOKEN);

        // then
        String accessKey = BLACKLIST_KEY_PREFIX + accessJti;
        String refreshKey = BLACKLIST_KEY_PREFIX + refreshJti;

        verify(valueOperations).set(accessKey, "blacklisted", Duration.ofSeconds(15 * 60)); // 15분
        verify(valueOperations).set(refreshKey, "blacklisted", Duration.ofSeconds(7 * 24 * 60 * 60)); // 7일
    }
}