package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.config.AuthProperties;
import com.jober.final2teamdrhong.config.JwtConfig;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.exception.AuthenticationException;
import com.jober.final2teamdrhong.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private SetOperations<String, String> setOperations;
    @Mock
    private AuthProperties authProperties;
    @Mock
    private AuthProperties.Token tokenProperties;
    @Mock
    private AuthProperties.Redis redisProperties;
    @Mock
    private AuthProperties.Messages messagesProperties;
    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_ACCESS_TOKEN = "test_access_token_123";
    private static final String TEST_REFRESH_TOKEN = "test_refresh_token_456";
    private static final String TEST_NEW_REFRESH_TOKEN = "new_refresh_token_789";
    private static final String TEST_TOKEN_HASH = "token_hash_abc";
    private static final String TEST_NEW_TOKEN_HASH = "new_token_hash_def";
    private static final Long TEST_USER_ID = 1L;
    private static final Integer TEST_USER_ID_INT = 1;
    private static final long TTL_SECONDS = 604800L; // 7일

    @BeforeEach
    void setUp() {
        // Common Redis mocks
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // Common AuthProperties.Token mocks
        lenient().when(authProperties.getToken()).thenReturn(tokenProperties);
        lenient().when(tokenProperties.getRefreshTokenValiditySeconds()).thenReturn(TTL_SECONDS);

        // Common AuthProperties.Redis mocks
        lenient().when(authProperties.getRedis()).thenReturn(redisProperties);
        lenient().when(redisProperties.getRefreshTokenKeyPrefix()).thenReturn("refresh_token:");
        lenient().when(redisProperties.getUserTokensKeyPrefix()).thenReturn("user_tokens:");

        // Common AuthProperties.Messages mocks
        lenient().when(authProperties.getMessages()).thenReturn(messagesProperties);
        lenient().when(messagesProperties.getInvalidRefreshToken()).thenReturn("유효하지 않은 리프레시 토큰입니다.");
        lenient().when(messagesProperties.getExpiredRefreshToken()).thenReturn("만료되었거나 유효하지 않은 리프레시 토큰입니다.");
        lenient().when(messagesProperties.getUserNotFound()).thenReturn("사용자를 찾을 수 없습니다.");
        lenient().when(messagesProperties.getInvalidTokenInfo()).thenReturn("토큰 정보가 유효하지 않습니다.");

        // CommonJwtConfig mocks
        lenient().when(jwtConfig.getUserIdFromToken(anyString())).thenReturn(TEST_USER_ID);
    }

    @Test
    @DisplayName("createRefreshToken: Refresh Token 생성 및 Redis 저장 성공 - 기존 토큰 무효화")
    void createRefreshToken_Success_RevokesExistingAndSavesNew() {
        // given
        User user = createTestUser();
        Set<String> existingTokens = Set.of("old_token_hash_1", "old_token_hash_2");

        given(setOperations.members("user_tokens:" + TEST_USER_ID)).willReturn(existingTokens);
        given(jwtConfig.generateRefreshToken(eq(TEST_EMAIL), eq(TEST_USER_ID_INT))).willReturn(TEST_REFRESH_TOKEN);
        given(jwtConfig.generateTokenHash(TEST_REFRESH_TOKEN)).willReturn(TEST_TOKEN_HASH);

        // when
        String result = refreshTokenService.createRefreshToken(user, TEST_IP);

        // then
        assertThat(result).isEqualTo(TEST_REFRESH_TOKEN);
        verify(redisTemplate).delete("refresh_token:old_token_hash_1");
        verify(redisTemplate).delete("refresh_token:old_token_hash_2");
        verify(redisTemplate).delete("user_tokens:" + TEST_USER_ID);
        verify(valueOperations).set("refresh_token:" + TEST_TOKEN_HASH, TEST_USER_ID.toString(), Duration.ofSeconds(TTL_SECONDS));
        verify(setOperations).add("user_tokens:" + TEST_USER_ID, TEST_TOKEN_HASH);
        verify(redisTemplate).expire("user_tokens:" + TEST_USER_ID, Duration.ofSeconds(TTL_SECONDS));
    }

    @Test
    @DisplayName("createRefreshToken: Redis 저장 실패 시 RuntimeException 발생")
    void createRefreshToken_RedisSaveFails_ThrowsRuntimeException() {
        // given
        User user = createTestUser();
        given(jwtConfig.generateRefreshToken(eq(TEST_EMAIL), eq(TEST_USER_ID_INT))).willReturn(TEST_REFRESH_TOKEN);
        given(jwtConfig.generateTokenHash(TEST_REFRESH_TOKEN)).willReturn(TEST_TOKEN_HASH);
        doThrow(new RuntimeException("Redis connection failed")).when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // when & then
        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(user, TEST_IP))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to save Refresh Token");
    }

    @Test
    @DisplayName("refreshTokens: 토큰 갱신 성공 - Token Rotation 적용")
    void refreshTokens_Success_AppliesTokenRotation() {
        // given
        User user = createTestUserWithVerifiedAuth();

        doNothing().when(rateLimitService).checkLoginRateLimit(TEST_IP);
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.isRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_EMAIL);
        given(jwtConfig.generateTokenHash(TEST_REFRESH_TOKEN)).willReturn(TEST_TOKEN_HASH);
        given(redisTemplate.hasKey("refresh_token:" + TEST_TOKEN_HASH)).willReturn(true);
        given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(jwtConfig.generateAccessToken(eq(TEST_EMAIL), eq(TEST_USER_ID_INT))).willReturn(TEST_ACCESS_TOKEN);
        given(jwtConfig.generateRefreshToken(eq(TEST_EMAIL), eq(TEST_USER_ID_INT))).willReturn(TEST_NEW_REFRESH_TOKEN);
        given(jwtConfig.generateTokenHash(TEST_NEW_REFRESH_TOKEN)).willReturn(TEST_NEW_TOKEN_HASH);

        // Mocks for revokeRefreshToken (called internally)
        given(valueOperations.get("refresh_token:" + TEST_TOKEN_HASH)).willReturn(TEST_USER_ID.toString());

        // when
        RefreshTokenService.TokenPair result = refreshTokenService.refreshTokens(TEST_REFRESH_TOKEN, TEST_IP);

        // then
        assertThat(result.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(TEST_NEW_REFRESH_TOKEN);
        verify(redisTemplate).delete("refresh_token:" + TEST_TOKEN_HASH); // Old token deletion
        verify(setOperations).remove("user_tokens:" + TEST_USER_ID, TEST_TOKEN_HASH); // Old token removed from set
        verify(valueOperations).set("refresh_token:" + TEST_NEW_TOKEN_HASH, TEST_USER_ID.toString(), Duration.ofSeconds(TTL_SECONDS)); // New token set
        verify(setOperations, times(1)).add(any(), any()); // New token added to set
        verify(redisTemplate).expire("user_tokens:" + TEST_USER_ID, Duration.ofSeconds(TTL_SECONDS)); // New token set expiration
    }

    @Test
    @DisplayName("refreshTokens: 유효하지 않은 Refresh Token으로 갱신 시 AuthenticationException 발생")
    void refreshTokens_InvalidRefreshToken_ThrowsAuthenticationException() {
        // given
        doNothing().when(rateLimitService).checkLoginRateLimit(TEST_IP);
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(false);
        given(messagesProperties.getInvalidRefreshToken()).willReturn("유효하지 않은 리프레시 토큰입니다.");

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refreshTokens(TEST_REFRESH_TOKEN, TEST_IP))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage(messagesProperties.getInvalidRefreshToken());
    }

    @Test
    @DisplayName("refreshTokens: 토큰에서 사용자 정보 추출 실패 시 AuthenticationException 발생")
    void refreshTokens_UserInfoExtractionFails_ThrowsAuthenticationException() {
        // given
        doNothing().when(rateLimitService).checkLoginRateLimit(TEST_IP);
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.isRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(null); // Simulate extraction failure
        given(messagesProperties.getInvalidTokenInfo()).willReturn("토큰 정보가 유효하지 않습니다.");

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refreshTokens(TEST_REFRESH_TOKEN, TEST_IP))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage(messagesProperties.getInvalidTokenInfo());
    }

    @Test
    @DisplayName("refreshTokens: Redis에 토큰이 존재하지 않을 때 AuthenticationException 발생")
    void refreshTokens_TokenNotExistsInRedis_ThrowsAuthenticationException() {
        // given
        User user = createTestUserWithVerifiedAuth();
        doNothing().when(rateLimitService).checkLoginRateLimit(TEST_IP);
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.isRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_EMAIL);
        given(jwtConfig.generateTokenHash(TEST_REFRESH_TOKEN)).willReturn(TEST_TOKEN_HASH);
        given(redisTemplate.hasKey("refresh_token:" + TEST_TOKEN_HASH)).willReturn(false); // Simulate not found in Redis
        given(messagesProperties.getExpiredRefreshToken()).willReturn("만료되었거나 유효하지 않은 리프레시 토큰입니다.");

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refreshTokens(TEST_REFRESH_TOKEN, TEST_IP))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage(messagesProperties.getExpiredRefreshToken());
    }

    @Test
    @DisplayName("refreshTokens: 사용자를 찾을 수 없을 때 AuthenticationException 발생")
    void refreshTokens_UserNotFound_ThrowsAuthenticationException() {
        // given
        doNothing().when(rateLimitService).checkLoginRateLimit(TEST_IP);
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.isRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_EMAIL);
        given(jwtConfig.generateTokenHash(TEST_REFRESH_TOKEN)).willReturn(TEST_TOKEN_HASH);
        given(redisTemplate.hasKey("refresh_token:" + TEST_TOKEN_HASH)).willReturn(true);
        given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.empty()); // Simulate user not found
        given(messagesProperties.getUserNotFound()).willReturn("사용자를 찾을 수 없습니다.");

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refreshTokens(TEST_REFRESH_TOKEN, TEST_IP))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage(messagesProperties.getUserNotFound());
    }

    @Test
    @DisplayName("refreshTokens: 미인증 사용자 토큰 갱신 시 AuthenticationException 발생")
    void refreshTokens_UserNotVerified_ThrowsAuthenticationException() {
        // given
        User unverifiedUser = createTestUser(); // User without verified auth
        doNothing().when(rateLimitService).checkLoginRateLimit(TEST_IP);
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.isRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_EMAIL);
        given(jwtConfig.generateTokenHash(TEST_REFRESH_TOKEN)).willReturn(TEST_TOKEN_HASH);
        given(redisTemplate.hasKey("refresh_token:" + TEST_TOKEN_HASH)).willReturn(true);
        given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(unverifiedUser));

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refreshTokens(TEST_REFRESH_TOKEN, TEST_IP))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("이메일 인증이 완료되지 않은 사용자입니다.");
    }

    @Test
    @DisplayName("revokeRefreshToken: 특정 Refresh Token 무효화 성공")
    void revokeRefreshToken_Success() {
        // given
        given(jwtConfig.generateTokenHash(TEST_REFRESH_TOKEN)).willReturn(TEST_TOKEN_HASH);
        given(valueOperations.get("refresh_token:" + TEST_TOKEN_HASH)).willReturn(TEST_USER_ID.toString());

        // when
        refreshTokenService.revokeRefreshToken(TEST_REFRESH_TOKEN);

        // then
        verify(redisTemplate).delete("refresh_token:" + TEST_TOKEN_HASH);
        verify(setOperations).remove("user_tokens:" + TEST_USER_ID, TEST_TOKEN_HASH);
    }

    @Test
    @DisplayName("revokeRefreshToken: null 토큰 전달 시 아무 작업도 수행하지 않음")
    void revokeRefreshToken_NullToken_DoesNothing() {
        // when
        refreshTokenService.revokeRefreshToken(null);

        // then
        verifyNoInteractions(jwtConfig); // No JWT processing for null token
        verifyNoInteractions(redisTemplate); // No Redis interaction for null token
    }

    @Test
    @DisplayName("revokeAllUserTokens: 사용자의 모든 Refresh Token 무효화 성공")
    void revokeAllUserTokens_Success() {
        // given
        Set<String> userTokens = Set.of("token_hash_1", "token_hash_2");
        given(setOperations.members("user_tokens:" + TEST_USER_ID)).willReturn(userTokens);

        // when
        refreshTokenService.revokeAllUserTokens(TEST_USER_ID);

        // then
        verify(redisTemplate).delete("refresh_token:token_hash_1");
        verify(redisTemplate).delete("refresh_token:token_hash_2");
        verify(redisTemplate).delete("user_tokens:" + TEST_USER_ID);
    }

    @Test
    @DisplayName("revokeAllUserTokens: 토큰이 없는 사용자에게 호출 시 아무 작업도 수행하지 않음")
    void revokeAllUserTokens_UserWithNoTokens_DoesNothing() {
        // given
        given(setOperations.members("user_tokens:" + TEST_USER_ID)).willReturn(Set.of());

        // when
        refreshTokenService.revokeAllUserTokens(TEST_USER_ID);

        // then
        verify(redisTemplate, never()).delete(anyString());
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("isValidRefreshToken: 유효한 Refresh Token 검증 성공")
    void isValidRefreshToken_ValidToken_ReturnsTrue() {
        // given
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.isRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.generateTokenHash(TEST_REFRESH_TOKEN)).willReturn(TEST_TOKEN_HASH);
        given(redisTemplate.hasKey("refresh_token:" + TEST_TOKEN_HASH)).willReturn(true);

        // when
        boolean result = refreshTokenService.isValidRefreshToken(TEST_REFRESH_TOKEN);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isValidRefreshToken: null 토큰 검증 시 false 반환")
    void isValidRefreshToken_NullToken_ReturnsFalse() {
        // when
        boolean result = refreshTokenService.isValidRefreshToken(null);
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValidRefreshToken: JWT 검증 실패한 토큰 시 false 반환")
    void isValidRefreshToken_InvalidJwt_ReturnsFalse() {
        // given
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(false);
        // when
        boolean result = refreshTokenService.isValidRefreshToken(TEST_REFRESH_TOKEN);
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValidRefreshToken: Access Token으로 검증 시 false 반환")
    void isValidRefreshToken_AccessToken_ReturnsFalse() {
        // given
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.isRefreshToken(TEST_REFRESH_TOKEN)).willReturn(false); // Simulate Access Token
        // when
        boolean result = refreshTokenService.isValidRefreshToken(TEST_REFRESH_TOKEN);
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValidRefreshToken: Redis에 존재하지 않는 토큰 시 false 반환")
    void isValidRefreshToken_TokenNotInRedis_ReturnsFalse() {
        // given
        given(jwtConfig.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.isRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtConfig.generateTokenHash(TEST_REFRESH_TOKEN)).willReturn(TEST_TOKEN_HASH);
        given(redisTemplate.hasKey("refresh_token:" + TEST_TOKEN_HASH)).willReturn(false); // Simulate not found in Redis
        // when
        boolean result = refreshTokenService.isValidRefreshToken(TEST_REFRESH_TOKEN);
        // then
        assertThat(result).isFalse();
    }

    // Helper methods
    private User createTestUser() {
        User user = User.create("테스트사용자", TEST_EMAIL, "010-1234-5678");
        try {
            var field = user.getClass().getDeclaredField("userId");
            field.setAccessible(true);
            field.set(user, TEST_USER_ID_INT);
        } catch (Exception e) {
            fail("테스트 유저 생성 실패: " + e.getMessage());
        }
        return user;
    }

    private User createTestUserWithVerifiedAuth() {
        User user = createTestUser();
        UserAuth verifiedAuth = UserAuth.createLocalAuth(user, "hashed_password");
        try {
            var field = verifiedAuth.getClass().getDeclaredField("isVerified");
            field.setAccessible(true);
            field.set(verifiedAuth, true);
        } catch (Exception e) {
            fail("인증된 테스트 유저 생성 실패: " + e.getMessage());
        }
        user.addUserAuth(verifiedAuth);
        return user;
    }
}