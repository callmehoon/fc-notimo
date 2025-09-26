package com.jober.final2teamdrhong.config;

import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * JwtConfig 단위 테스트
 * 실제 JWT 토큰 생성, 검증, Claims 추출 로직을 직접 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
class JwtConfigTest {

    @Mock
    private AuthProperties authProperties;

    @Mock
    private AuthProperties.Token tokenProperties;

    private JwtConfig jwtConfig;

    // 테스트용 상수
    private static final String TEST_JWT_SECRET = "test-jwt-secret-key-for-unit-testing-must-be-at-least-32-characters-long";
    private static final String TEST_EMAIL = "test@example.com";
    private static final Integer TEST_USER_ID = 1;
    private static final long ACCESS_TOKEN_VALIDITY = 900; // 15분
    private static final long REFRESH_TOKEN_VALIDITY = 604800; // 7일

    @BeforeEach
    void setUp() {
        // AuthProperties Mock 설정 (lenient로 설정하여 불필요한 stubbings 허용)
        lenient().when(authProperties.getToken()).thenReturn(tokenProperties);
        lenient().when(tokenProperties.getAccessTokenValiditySeconds()).thenReturn(ACCESS_TOKEN_VALIDITY);
        lenient().when(tokenProperties.getRefreshTokenValiditySeconds()).thenReturn(REFRESH_TOKEN_VALIDITY);

        // JwtConfig 인스턴스 생성 및 시크릿 키 설정
        jwtConfig = new JwtConfig(authProperties);
        ReflectionTestUtils.setField(jwtConfig, "jwtSecretKey", TEST_JWT_SECRET);
    }

    @Nested
    @DisplayName("JWT 설정 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 JWT 시크릿 키로 검증 성공")
        void validateJwtSecret_ValidKey_Success() {
            // when & then - 예외가 발생하지 않아야 함
            assertThatCode(() -> jwtConfig.validateJwtSecret())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("JWT 시크릿 키가 null인 경우 예외 발생")
        void validateJwtSecret_NullKey_ThrowsException() {
            // given
            ReflectionTestUtils.setField(jwtConfig, "jwtSecretKey", null);

            // when & then
            assertThatThrownBy(() -> jwtConfig.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("JWT secret key must be configured");
        }

        @Test
        @DisplayName("JWT 시크릿 키가 32자 미만인 경우 예외 발생")
        void validateJwtSecret_ShortKey_ThrowsException() {
            // given
            ReflectionTestUtils.setField(jwtConfig, "jwtSecretKey", "short-key");

            // when & then
            assertThatThrownBy(() -> jwtConfig.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("JWT secret key must be at least 32 characters");
        }

        @Test
        @DisplayName("기본 JWT 시크릿 키 사용 시 예외 발생")
        void validateJwtSecret_DefaultKey_ThrowsException() {
            // given
            String defaultKey = "your-super-super-long-and-secure-secret-key-for-jwt-hs256";
            ReflectionTestUtils.setField(jwtConfig, "jwtSecretKey", defaultKey);

            // when & then
            assertThatThrownBy(() -> jwtConfig.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Default JWT secret key detected. Please use a secure, unique key");
        }
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class TokenGenerationTest {

        @Test
        @DisplayName("Access Token 생성 성공")
        void generateAccessToken_Success() {
            // when
            String token = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);

            // then
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3); // JWT는 3개 파트로 구성

            // 생성된 토큰의 내용 검증
            assertThat(jwtConfig.validateToken(token)).isTrue();
            assertThat(jwtConfig.getEmailFromToken(token)).isEqualTo(TEST_EMAIL);
            assertThat(jwtConfig.getUserIdFromToken(token)).isEqualTo(TEST_USER_ID.longValue());
            assertThat(jwtConfig.getTokenType(token)).isEqualTo("access");
            assertThat(jwtConfig.isAccessToken(token)).isTrue();
            assertThat(jwtConfig.isRefreshToken(token)).isFalse();
        }

        @Test
        @DisplayName("Refresh Token 생성 성공")
        void generateRefreshToken_Success() {
            // when
            String token = jwtConfig.generateRefreshToken(TEST_EMAIL, TEST_USER_ID);

            // then
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);

            // 생성된 토큰의 내용 검증
            assertThat(jwtConfig.validateToken(token)).isTrue();
            assertThat(jwtConfig.getEmailFromToken(token)).isEqualTo(TEST_EMAIL);
            assertThat(jwtConfig.getUserIdFromToken(token)).isEqualTo(TEST_USER_ID.longValue());
            assertThat(jwtConfig.getTokenType(token)).isEqualTo("refresh");
            assertThat(jwtConfig.isRefreshToken(token)).isTrue();
            assertThat(jwtConfig.isAccessToken(token)).isFalse();
        }

        @Test
        @DisplayName("동일한 정보로 생성한 토큰들은 서로 다름 (JTI 고유성)")
        void generateToken_UniqueJti() {
            // when
            String token1 = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);
            String token2 = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);

            // then
            assertThat(token1).isNotEqualTo(token2);
            assertThat(jwtConfig.getJtiFromToken(token1)).isNotEqualTo(jwtConfig.getJtiFromToken(token2));
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class TokenValidationTest {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void validateToken_ValidToken_ReturnsTrue() {
            // given
            String token = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);

            // when
            boolean isValid = jwtConfig.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("잘못된 형식의 토큰 검증 실패")
        void validateToken_MalformedToken_ReturnsFalse() {
            // given
            String malformedToken = "invalid.token.format";

            // when
            boolean isValid = jwtConfig.validateToken(malformedToken);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("null 토큰 검증 실패")
        void validateToken_NullToken_ReturnsFalse() {
            // when
            boolean isValid = jwtConfig.validateToken(null);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰 검증 실패")
        void validateToken_EmptyToken_ReturnsFalse() {
            // when
            boolean isValid = jwtConfig.validateToken("");

            // then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Claims 추출 테스트")
    class ClaimsExtractionTest {

        private String testToken;

        @BeforeEach
        void setUpToken() {
            testToken = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);
        }

        @Test
        @DisplayName("토큰에서 Claims 추출 성공")
        void getClaimsFromToken_ValidToken_ReturnsClaims() {
            // when
            Claims claims = jwtConfig.getClaimsFromToken(testToken);

            // then
            assertThat(claims).isNotNull();
            assertThat(claims.getSubject()).isEqualTo(TEST_EMAIL);
            assertThat(claims.get("userId")).isEqualTo(TEST_USER_ID);
            assertThat(claims.get("tokenType")).isEqualTo("access");
            assertThat(claims.getId()).isNotNull();
            assertThat(claims.getExpiration()).isAfter(new Date());
        }

        @Test
        @DisplayName("토큰에서 이메일 추출 성공")
        void getEmailFromToken_ValidToken_ReturnsEmail() {
            // when
            String email = jwtConfig.getEmailFromToken(testToken);

            // then
            assertThat(email).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("토큰에서 사용자 ID 추출 성공")
        void getUserIdFromToken_ValidToken_ReturnsUserId() {
            // when
            Long userId = jwtConfig.getUserIdFromToken(testToken);

            // then
            assertThat(userId).isEqualTo(TEST_USER_ID.longValue());
        }

        @Test
        @DisplayName("토큰에서 JTI 추출 성공")
        void getJtiFromToken_ValidToken_ReturnsJti() {
            // when
            String jti = jwtConfig.getJtiFromToken(testToken);

            // then
            assertThat(jti).isNotNull();
            assertThat(jti).hasSize(36); // UUID 형식
        }

        @Test
        @DisplayName("토큰에서 만료 시간 추출 성공")
        void getExpirationDateFromToken_ValidToken_ReturnsExpiration() {
            // when
            Date expiration = jwtConfig.getExpirationDateFromToken(testToken);

            // then
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());

            // Access Token 유효 기간 확인 (오차 범위 10초)
            long expectedExpiration = System.currentTimeMillis() + (ACCESS_TOKEN_VALIDITY * 1000);
            assertThat(expiration.getTime()).isBetween(
                expectedExpiration - 10000, // 10초 전
                expectedExpiration + 10000  // 10초 후
            );
        }

        @Test
        @DisplayName("잘못된 토큰에서 Claims 추출 시 null 반환")
        void getClaimsFromToken_InvalidToken_ReturnsNull() {
            // when
            Claims claims = jwtConfig.getClaimsFromToken("invalid.token");

            // then
            assertThat(claims).isNull();
        }
    }

    @Nested
    @DisplayName("토큰 타입 확인 테스트")
    class TokenTypeTest {

        @Test
        @DisplayName("Access Token 타입 확인")
        void isAccessToken_AccessToken_ReturnsTrue() {
            // given
            String accessToken = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);

            // when & then
            assertThat(jwtConfig.isAccessToken(accessToken)).isTrue();
            assertThat(jwtConfig.isRefreshToken(accessToken)).isFalse();
            assertThat(jwtConfig.getTokenType(accessToken)).isEqualTo("access");
        }

        @Test
        @DisplayName("Refresh Token 타입 확인")
        void isRefreshToken_RefreshToken_ReturnsTrue() {
            // given
            String refreshToken = jwtConfig.generateRefreshToken(TEST_EMAIL, TEST_USER_ID);

            // when & then
            assertThat(jwtConfig.isRefreshToken(refreshToken)).isTrue();
            assertThat(jwtConfig.isAccessToken(refreshToken)).isFalse();
            assertThat(jwtConfig.getTokenType(refreshToken)).isEqualTo("refresh");
        }
    }

    @Nested
    @DisplayName("토큰 만료 확인 테스트")
    class TokenExpirationTest {

        @Test
        @DisplayName("유효한 토큰은 만료되지 않음")
        void isTokenExpired_ValidToken_ReturnsFalse() {
            // given
            String token = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);

            // when
            boolean isExpired = jwtConfig.isTokenExpired(token);

            // then
            assertThat(isExpired).isFalse();
        }

        @Test
        @DisplayName("잘못된 토큰에 대해서는 만료 확인이 정상 동작")
        void isTokenExpired_InvalidToken_HandledGracefully() {
            // when
            boolean isExpired = jwtConfig.isTokenExpired("invalid.token");

            // then - 예외가 발생하지 않고 처리됨
            assertThat(isExpired).isFalse(); // getExpirationDateFromToken이 null을 반환하므로
        }
    }

    @Nested
    @DisplayName("Authorization 헤더 처리 테스트")
    class AuthorizationHeaderTest {

        @Test
        @DisplayName("유효한 Bearer 토큰 추출 성공")
        void extractTokenFromHeader_ValidBearerToken_ReturnsToken() {
            // given
            String token = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);
            String authHeader = "Bearer " + token;

            // when
            String extractedToken = jwtConfig.extractTokenFromHeader(authHeader);

            // then
            assertThat(extractedToken).isEqualTo(token);
        }

        @Test
        @DisplayName("Bearer 접두사 없는 헤더에서 null 반환")
        void extractTokenFromHeader_NoBearerPrefix_ReturnsNull() {
            // given
            String authHeader = "SomeToken";

            // when
            String extractedToken = jwtConfig.extractTokenFromHeader(authHeader);

            // then
            assertThat(extractedToken).isNull();
        }

        @Test
        @DisplayName("null 헤더에서 null 반환")
        void extractTokenFromHeader_NullHeader_ReturnsNull() {
            // when
            String extractedToken = jwtConfig.extractTokenFromHeader(null);

            // then
            assertThat(extractedToken).isNull();
        }

        @Test
        @DisplayName("빈 헤더에서 null 반환")
        void extractTokenFromHeader_EmptyHeader_ReturnsNull() {
            // when
            String extractedToken = jwtConfig.extractTokenFromHeader("");

            // then
            assertThat(extractedToken).isNull();
        }
    }

    @Nested
    @DisplayName("JwtClaims 객체 생성 테스트")
    class JwtClaimsCreationTest {

        @Test
        @DisplayName("유효한 토큰으로 JwtClaims 생성 성공")
        void getJwtClaims_ValidToken_ReturnsJwtClaims() {
            // given
            String token = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);

            // when
            JwtClaims jwtClaims = jwtConfig.getJwtClaims(token);

            // then
            assertThat(jwtClaims).isNotNull();
            assertThat(jwtClaims.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(jwtClaims.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(jwtClaims.getTokenType()).isEqualTo("access");
            assertThat(jwtClaims.getJti()).isNotNull();
            assertThat(jwtClaims.getExpiresAt()).isAfter(java.time.LocalDateTime.now());
        }

        @Test
        @DisplayName("Authorization 헤더에서 JwtClaims 생성 성공")
        void getJwtClaimsFromHeader_ValidHeader_ReturnsJwtClaims() {
            // given
            String token = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);
            String authHeader = "Bearer " + token;

            // when
            JwtClaims jwtClaims = jwtConfig.getJwtClaimsFromHeader(authHeader);

            // then
            assertThat(jwtClaims).isNotNull();
            assertThat(jwtClaims.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(jwtClaims.getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("잘못된 토큰으로 JwtClaims 생성 시 null 반환")
        void getJwtClaims_InvalidToken_ReturnsNull() {
            // when
            JwtClaims jwtClaims = jwtConfig.getJwtClaims("invalid.token");

            // then
            assertThat(jwtClaims).isNull();
        }
    }

    @Nested
    @DisplayName("토큰 해시 생성 테스트")
    class TokenHashTest {

        @Test
        @DisplayName("토큰 해시 생성 성공")
        void generateTokenHash_ValidToken_ReturnsHash() {
            // given
            String token = jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID);

            // when
            String hash = jwtConfig.generateTokenHash(token);

            // then
            assertThat(hash).isNotNull();
            assertThat(hash).isNotEmpty();

            // 동일한 토큰은 동일한 해시 생성
            String hash2 = jwtConfig.generateTokenHash(token);
            assertThat(hash).isEqualTo(hash2);

            // 다른 토큰은 다른 해시 생성
            String otherToken = jwtConfig.generateAccessToken("other@example.com", 2);
            String otherHash = jwtConfig.generateTokenHash(otherToken);
            assertThat(hash).isNotEqualTo(otherHash);
        }
    }

    @Nested
    @DisplayName("설정값 반환 테스트")
    class ConfigurationTest {

        @Test
        @DisplayName("Access Token 유효 기간 반환")
        void getAccessTokenValiditySeconds_ReturnsConfiguredValue() {
            // when
            long validity = jwtConfig.getAccessTokenValiditySeconds();

            // then
            assertThat(validity).isEqualTo(ACCESS_TOKEN_VALIDITY);
        }

        @Test
        @DisplayName("Refresh Token 유효 기간 반환")
        void getRefreshTokenValiditySeconds_ReturnsConfiguredValue() {
            // when
            long validity = jwtConfig.getRefreshTokenValiditySeconds();

            // then
            assertThat(validity).isEqualTo(REFRESH_TOKEN_VALIDITY);
        }
    }
}