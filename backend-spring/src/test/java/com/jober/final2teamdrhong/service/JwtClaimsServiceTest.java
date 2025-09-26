package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.config.JwtConfig;
import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.exception.AuthenticationException;
import com.jober.final2teamdrhong.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * JwtClaimsService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class JwtClaimsServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private JwtClaimsService jwtClaimsService;

    // 테스트 상수
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_EMAIL = "test@example.com";
    private static final Integer TEST_USER_ID = 1;
    private static final String TEST_USER_NAME = "테스트사용자";
    private static final String TEST_JTI = "test-jti-12345";
    private static final String BEARER_TOKEN = "Bearer " + TEST_TOKEN;
    private static final LocalDateTime TEST_EXPIRES_AT = LocalDateTime.now().plusMinutes(15);

    private JwtClaims testBasicClaims;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 기본 JWT Claims 생성 (JWT에서 추출되는 정보)
        testBasicClaims = JwtClaims.builder()
                .email(TEST_EMAIL)
                .userId(TEST_USER_ID)
                .tokenType("access")
                .jti(TEST_JTI)
                .expiresAt(TEST_EXPIRES_AT)
                // DB 정보는 아직 없음
                .build();

        // 테스트용 User 엔터티
        testUser = User.builder()
                .userId(TEST_USER_ID)
                .userEmail(TEST_EMAIL)
                .userName(TEST_USER_NAME)
                .userRole(User.UserRole.USER)
                .build();
    }

    @Nested
    @DisplayName("getEnrichedClaims 메서드 테스트")
    class GetEnrichedClaimsTest {

        @Test
        @DisplayName("유효한 토큰으로 Claims 추출 및 DB 정보 보완 성공")
        void getEnrichedClaims_ValidToken_ReturnsEnrichedClaims() {
            // given
            given(jwtConfig.getJwtClaims(TEST_TOKEN)).willReturn(testBasicClaims);
            given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));

            // when
            JwtClaims result = jwtClaimsService.getEnrichedClaims(TEST_TOKEN);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getUserName()).isEqualTo(TEST_USER_NAME);
            assertThat(result.getUserRole()).isEqualTo(User.UserRole.USER);
            assertThat(result.getTokenType()).isEqualTo("access");
            assertThat(result.getJti()).isEqualTo(TEST_JTI);
            assertThat(result.getExpiresAt()).isEqualTo(TEST_EXPIRES_AT);

            verify(jwtConfig).getJwtClaims(TEST_TOKEN);
            verify(userRepository).findByUserEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("JWT Config에서 null 반환 시 AuthenticationException")
        void getEnrichedClaims_JwtConfigReturnsNull_ThrowsException() {
            // given
            given(jwtConfig.getJwtClaims(TEST_TOKEN)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> jwtClaimsService.getEnrichedClaims(TEST_TOKEN))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("유효하지 않은 토큰입니다");

            verify(userRepository, never()).findByUserEmail(anyString());
        }

        @Test
        @DisplayName("JWT Config에서 예외 발생 시 예외 전파")
        void getEnrichedClaims_JwtConfigThrowsException_PropagatesException() {
            // given
            RuntimeException jwtException = new RuntimeException("JWT 파싱 실패");
            given(jwtConfig.getJwtClaims(TEST_TOKEN)).willThrow(jwtException);

            // when & then
            assertThatThrownBy(() -> jwtClaimsService.getEnrichedClaims(TEST_TOKEN))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("JWT 파싱 실패");

            verify(userRepository, never()).findByUserEmail(anyString());
        }
    }

    @Nested
    @DisplayName("getEnrichedClaimsFromHeader 메서드 테스트")
    class GetEnrichedClaimsFromHeaderTest {

        @Test
        @DisplayName("유효한 Authorization 헤더로 Claims 추출 성공")
        void getEnrichedClaimsFromHeader_ValidHeader_ReturnsEnrichedClaims() {
            // given
            given(jwtConfig.extractTokenFromHeader(BEARER_TOKEN)).willReturn(TEST_TOKEN);
            given(jwtConfig.getJwtClaims(TEST_TOKEN)).willReturn(testBasicClaims);
            given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));

            // when
            JwtClaims result = jwtClaimsService.getEnrichedClaimsFromHeader(BEARER_TOKEN);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(result.getUserName()).isEqualTo(TEST_USER_NAME);

            verify(jwtConfig).extractTokenFromHeader(BEARER_TOKEN);
            verify(jwtConfig).getJwtClaims(TEST_TOKEN);
            verify(userRepository).findByUserEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("토큰 추출 실패 시 AuthenticationException")
        void getEnrichedClaimsFromHeader_TokenExtractionFails_ThrowsException() {
            // given
            given(jwtConfig.extractTokenFromHeader(BEARER_TOKEN)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> jwtClaimsService.getEnrichedClaimsFromHeader(BEARER_TOKEN))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("유효하지 않은 Authorization 헤더입니다");

            verify(jwtConfig, never()).getJwtClaims(anyString());
            verify(userRepository, never()).findByUserEmail(anyString());
        }

        @Test
        @DisplayName("잘못된 헤더 형식 처리")
        void getEnrichedClaimsFromHeader_InvalidHeaderFormat_ThrowsException() {
            // given
            String invalidHeader = "InvalidHeaderFormat";
            given(jwtConfig.extractTokenFromHeader(invalidHeader)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> jwtClaimsService.getEnrichedClaimsFromHeader(invalidHeader))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("유효하지 않은 Authorization 헤더입니다");
        }
    }

    @Nested
    @DisplayName("enrichWithUserInfo 메서드 테스트")
    class EnrichWithUserInfoTest {

        @Test
        @DisplayName("기본 Claims에 DB 사용자 정보 추가 성공")
        void enrichWithUserInfo_ValidClaims_EnrichesWithDbInfo() {
            // given
            given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));

            // when
            JwtClaims result = jwtClaimsService.enrichWithUserInfo(testBasicClaims);

            // then
            assertThat(result).isNotNull();
            // JWT 정보 유지
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getTokenType()).isEqualTo("access");
            assertThat(result.getJti()).isEqualTo(TEST_JTI);
            assertThat(result.getExpiresAt()).isEqualTo(TEST_EXPIRES_AT);
            // DB 정보 추가
            assertThat(result.getUserName()).isEqualTo(TEST_USER_NAME);
            assertThat(result.getUserRole()).isEqualTo(User.UserRole.USER);

            verify(userRepository).findByUserEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("ADMIN 권한 사용자 정보 보완")
        void enrichWithUserInfo_AdminUser_EnrichesWithAdminRole() {
            // given
            User adminUser = User.builder()
                    .userId(TEST_USER_ID)
                    .userEmail(TEST_EMAIL)
                    .userName("관리자")
                    .userRole(User.UserRole.ADMIN)
                    .build();

            given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(adminUser));

            // when
            JwtClaims result = jwtClaimsService.enrichWithUserInfo(testBasicClaims);

            // then
            assertThat(result.getUserRole()).isEqualTo(User.UserRole.ADMIN);
            assertThat(result.getUserName()).isEqualTo("관리자");
            assertThat(result.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("이메일이 null인 경우 AuthenticationException")
        void enrichWithUserInfo_NullEmail_ThrowsException() {
            // given
            JwtClaims claimsWithNullEmail = JwtClaims.builder()
                    .email(null)
                    .userId(TEST_USER_ID)
                    .tokenType("access")
                    .build();

            // when & then
            assertThatThrownBy(() -> jwtClaimsService.enrichWithUserInfo(claimsWithNullEmail))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("토큰에 필수 정보가 누락되었습니다");

            verify(userRepository, never()).findByUserEmail(anyString());
        }

        @Test
        @DisplayName("userId가 null인 경우 AuthenticationException")
        void enrichWithUserInfo_NullUserId_ThrowsException() {
            // given
            JwtClaims claimsWithNullUserId = JwtClaims.builder()
                    .email(TEST_EMAIL)
                    .userId(null)
                    .tokenType("access")
                    .build();

            // when & then
            assertThatThrownBy(() -> jwtClaimsService.enrichWithUserInfo(claimsWithNullUserId))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("토큰에 필수 정보가 누락되었습니다");

            verify(userRepository, never()).findByUserEmail(anyString());
        }

        @Test
        @DisplayName("사용자를 찾을 수 없는 경우 AuthenticationException")
        void enrichWithUserInfo_UserNotFound_ThrowsException() {
            // given
            given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> jwtClaimsService.enrichWithUserInfo(testBasicClaims))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("사용자를 찾을 수 없습니다");

            verify(userRepository).findByUserEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("토큰의 userId와 DB의 userId 불일치 시 AuthenticationException")
        void enrichWithUserInfo_UserIdMismatch_ThrowsException() {
            // given
            User differentUser = User.builder()
                    .userId(999) // 다른 userId
                    .userEmail(TEST_EMAIL)
                    .userName(TEST_USER_NAME)
                    .userRole(User.UserRole.USER)
                    .build();

            given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(differentUser));

            // when & then
            assertThatThrownBy(() -> jwtClaimsService.enrichWithUserInfo(testBasicClaims))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("사용자 정보가 일치하지 않습니다");

            verify(userRepository).findByUserEmail(TEST_EMAIL);
        }
    }

    @Nested
    @DisplayName("캐시 관리 메서드 테스트")
    class CacheManagementTest {

        @Test
        @DisplayName("사용자 정보 캐시 무효화")
        void evictUserInfoCache_ValidParameters_CacheEvicted() {
            // when
            jwtClaimsService.evictUserInfoCache(TEST_EMAIL, TEST_USER_ID);

            // then - 로그 출력이 정상적으로 수행됨을 확인 (실제 캐시 무효화는 Spring AOP에 의해 처리)
            // 이 테스트에서는 메서드가 예외 없이 실행되는지 확인
            assertThatCode(() -> jwtClaimsService.evictUserInfoCache(TEST_EMAIL, TEST_USER_ID))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("모든 사용자 정보 캐시 무효화")
        void evictAllUserInfoCache_ExecutedSuccessfully() {
            // when
            jwtClaimsService.evictAllUserInfoCache();

            // then - 메서드가 예외 없이 실행되는지 확인
            assertThatCode(() -> jwtClaimsService.evictAllUserInfoCache())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 값으로 캐시 무효화 호출 시 정상 처리")
        void evictUserInfoCache_NullParameters_HandledGracefully() {
            // when & then - null 값이어도 예외가 발생하지 않아야 함
            assertThatCode(() -> jwtClaimsService.evictUserInfoCache(null, null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("편의 메서드 테스트")
    class ConvenienceMethodsTest {

        @Test
        @DisplayName("isAdmin 메서드 - ADMIN 권한 사용자")
        void jwtClaims_isAdmin_AdminUser_ReturnsTrue() {
            // given
            JwtClaims adminClaims = JwtClaims.builder()
                    .email(TEST_EMAIL)
                    .userId(TEST_USER_ID)
                    .userName("관리자")
                    .userRole(User.UserRole.ADMIN)
                    .build();

            // when & then
            assertThat(adminClaims.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("isAdmin 메서드 - USER 권한 사용자")
        void jwtClaims_isAdmin_RegularUser_ReturnsFalse() {
            // given
            JwtClaims userClaims = JwtClaims.builder()
                    .email(TEST_EMAIL)
                    .userId(TEST_USER_ID)
                    .userName("일반사용자")
                    .userRole(User.UserRole.USER)
                    .build();

            // when & then
            assertThat(userClaims.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("isAccessToken 메서드 - Access Token")
        void jwtClaims_isAccessToken_AccessTokenType_ReturnsTrue() {
            // given
            JwtClaims accessClaims = JwtClaims.builder()
                    .tokenType("access")
                    .build();

            // when & then
            assertThat(accessClaims.isAccessToken()).isTrue();
            assertThat(accessClaims.isRefreshToken()).isFalse();
        }

        @Test
        @DisplayName("isRefreshToken 메서드 - Refresh Token")
        void jwtClaims_isRefreshToken_RefreshTokenType_ReturnsTrue() {
            // given
            JwtClaims refreshClaims = JwtClaims.builder()
                    .tokenType("refresh")
                    .build();

            // when & then
            assertThat(refreshClaims.isRefreshToken()).isTrue();
            assertThat(refreshClaims.isAccessToken()).isFalse();
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("전체 Claims 처리 플로우 - 토큰에서 완전한 사용자 정보까지")
        void integrationScenario_CompleteClaimsProcessingFlow() {
            // given
            given(jwtConfig.extractTokenFromHeader(BEARER_TOKEN)).willReturn(TEST_TOKEN);
            given(jwtConfig.getJwtClaims(TEST_TOKEN)).willReturn(testBasicClaims);
            given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));

            // when - Authorization 헤더부터 시작하여 완전한 Claims 얻기
            JwtClaims result = jwtClaimsService.getEnrichedClaimsFromHeader(BEARER_TOKEN);

            // then - 모든 정보가 올바르게 설정되었는지 확인
            assertThat(result).isNotNull();

            // JWT 기본 정보
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getTokenType()).isEqualTo("access");
            assertThat(result.getJti()).isEqualTo(TEST_JTI);
            assertThat(result.getExpiresAt()).isEqualTo(TEST_EXPIRES_AT);

            // DB에서 보완된 정보
            assertThat(result.getUserName()).isEqualTo(TEST_USER_NAME);
            assertThat(result.getUserRole()).isEqualTo(User.UserRole.USER);

            // 편의 메서드들
            assertThat(result.isAccessToken()).isTrue();
            assertThat(result.isRefreshToken()).isFalse();
            assertThat(result.isAdmin()).isFalse();

            // 호출 순서 확인
            verify(jwtConfig).extractTokenFromHeader(BEARER_TOKEN);
            verify(jwtConfig).getJwtClaims(TEST_TOKEN);
            verify(userRepository).findByUserEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("Refresh Token 처리 시나리오")
        void integrationScenario_RefreshTokenProcessing() {
            // given
            JwtClaims refreshBasicClaims = JwtClaims.builder()
                    .email(TEST_EMAIL)
                    .userId(TEST_USER_ID)
                    .tokenType("refresh")
                    .jti(TEST_JTI)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();

            given(jwtConfig.getJwtClaims(TEST_TOKEN)).willReturn(refreshBasicClaims);
            given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));

            // when
            JwtClaims result = jwtClaimsService.getEnrichedClaims(TEST_TOKEN);

            // then
            assertThat(result.getTokenType()).isEqualTo("refresh");
            assertThat(result.isRefreshToken()).isTrue();
            assertThat(result.isAccessToken()).isFalse();
        }

        @Test
        @DisplayName("관리자 사용자의 완전한 Claims 처리")
        void integrationScenario_AdminUserCompleteProcessing() {
            // given
            User adminUser = User.builder()
                    .userId(TEST_USER_ID)
                    .userEmail(TEST_EMAIL)
                    .userName("시스템 관리자")
                    .userRole(User.UserRole.ADMIN)
                    .build();

            given(jwtConfig.getJwtClaims(TEST_TOKEN)).willReturn(testBasicClaims);
            given(userRepository.findByUserEmail(TEST_EMAIL)).willReturn(Optional.of(adminUser));

            // when
            JwtClaims result = jwtClaimsService.getEnrichedClaims(TEST_TOKEN);

            // then
            assertThat(result.getUserRole()).isEqualTo(User.UserRole.ADMIN);
            assertThat(result.getUserName()).isEqualTo("시스템 관리자");
            assertThat(result.isAdmin()).isTrue();
        }
    }
}