package com.jober.final2teamdrhong.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.config.JwtConfig;
import com.jober.final2teamdrhong.dto.auth.OAuth2UserInfo;
import com.jober.final2teamdrhong.dto.auth.OAuth2UserInfoFactory;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * OAuth2AuthenticationSuccessHandler 단위 테스트
 *
 * 테스트 대상:
 * - OAuth2 로그인 성공 시 기존 회원/신규 회원 처리 로직
 * - JWT 토큰 발급 및 리다이렉트 로직
 * - 오류 처리 로직
 *
 * 테스트 방식:
 * - @ExtendWith(MockitoExtension.class)를 사용한 단위 테스트
 * - 모든 의존성을 Mock으로 처리
 * - Given-When-Then 패턴으로 명확한 테스트 구조 유지
 */
@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    // @Mock: Mockito가 가짜(Mock) 객체를 생성하여 주입합니다.
    // 이 객체들은 실제 로직을 수행하지 않으며, 오직 정의된 행동(stub)만 수행합니다.
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private TokenService tokenService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private RedirectStrategy redirectStrategy;

    @Mock
    private OAuth2UserInfo oAuth2UserInfo;

    // @InjectMocks: @Mock으로 생성된 가짜 객체들을 실제 테스트 대상인 클래스에 주입합니다.
    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    // 테스트 상수 정의
    private static final String TEST_EMAIL = "test@google.com";
    private static final String TEST_NAME = "구글사용자";
    private static final String TEST_SOCIAL_ID = "google_123456789";
    private static final String TEST_PROVIDER = "google";
    private static final Integer TEST_USER_ID = 1;
    private static final String TEST_USER_ROLE = "USER";
    private static final String TEST_ACCESS_TOKEN = "test.access.token";
    private static final String TEST_REFRESH_TOKEN = "test.refresh.token";
    private static final String TEST_CLIENT_IP = "127.0.0.1";
    private static final String TEST_REDIRECT_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        // 1. ReflectionTestUtils를 사용하여 private 필드 값을 설정합니다.
        ReflectionTestUtils.setField(successHandler, "allowedOrigins", TEST_REDIRECT_URL);

        // 2. RedirectStrategy Mock을 핸들러에 설정합니다.
        ReflectionTestUtils.setField(successHandler, "redirectStrategy", redirectStrategy);

        // 3. 공통 Mock 설정 - 모든 테스트에서 사용되는 기본 설정만 포함
        given(authentication.getPrincipal()).willReturn(oAuth2User);
    }

    @Nested
    @DisplayName("기존 회원 처리 테스트")
    class ExistingUserTest {

        @Test
        @DisplayName("기존 회원 OAuth2 로그인 성공 테스트")
        void shouldHandleExistingUserSuccessfully() throws IOException {
            // given
            // 1. OAuth2 사용자 정보에 기존 회원 정보를 설정합니다.
            Map<String, Object> attributes = Map.of(
                "isExistingUser", true,
                "provider", TEST_PROVIDER,
                "userId", TEST_USER_ID,
                "userRole", TEST_USER_ROLE
            );
            given(oAuth2User.getAttributes()).willReturn(attributes);

            // 2. 사용자 조회가 성공하도록 설정합니다.
            User existingUser = User.create(TEST_NAME, TEST_EMAIL, "010-1234-5678");
            ReflectionTestUtils.setField(existingUser, "userId", TEST_USER_ID);
            given(userRepository.findByIdWithAuth(TEST_USER_ID)).willReturn(Optional.of(existingUser));

            // 3. IP 주소 추출을 위해 request Mock 설정합니다.
            given(request.getRemoteAddr()).willReturn(TEST_CLIENT_IP);

            // 4. JWT 토큰 생성이 성공하도록 설정합니다.
            given(jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID)).willReturn(TEST_ACCESS_TOKEN);
            given(tokenService.createRefreshToken(existingUser, TEST_CLIENT_IP)).willReturn(TEST_REFRESH_TOKEN);

            // when
            // 1. OAuth2 로그인 성공 처리를 실행합니다.
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            // 1. 사용자 조회가 수행되었는지 확인합니다.
            then(userRepository).should(times(1)).findByIdWithAuth(TEST_USER_ID);

            // 2. JWT 토큰 생성이 수행되었는지 확인합니다.
            then(jwtConfig).should(times(1)).generateAccessToken(TEST_EMAIL, TEST_USER_ID);
            then(tokenService).should(times(1)).createRefreshToken(existingUser, TEST_CLIENT_IP);

            // 3. 올바른 URL로 리다이렉트되는지 확인합니다.
            String expectedUrl = TEST_REDIRECT_URL +
                "?success=true" +
                "&accessToken=" + TEST_ACCESS_TOKEN +
                "&refreshToken=" + TEST_REFRESH_TOKEN +
                "&isNewUser=false";
            then(redirectStrategy).should(times(1)).sendRedirect(request, response, expectedUrl);
        }

        @Test
        @DisplayName("기존 회원 처리 시 사용자 정보 불완전 오류 테스트")
        void shouldHandleIncompleteUserInfoError() throws IOException {
            // given
            // 1. 불완전한 사용자 정보를 설정합니다. (userId가 null)
            Map<String, Object> attributes = Map.of(
                "isExistingUser", true,
                "provider", TEST_PROVIDER,
                "userRole", TEST_USER_ROLE
                // userId가 누락됨
            );
            given(oAuth2User.getAttributes()).willReturn(attributes);

            // when
            // 1. OAuth2 로그인 성공 처리를 실행합니다.
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            // 1. 오류 처리로 에러 페이지로 리다이렉트되는지 확인합니다.
            // URLEncoder.encode()는 공백을 '+'로 인코딩하므로 이를 고려합니다.
            then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("/login"));
            then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("error=oauth2_error"));

            // 2. 사용자 조회는 수행되지 않았는지 확인합니다.
            then(userRepository).should(times(0)).findByIdWithAuth(any());
        }

        @Test
        @DisplayName("기존 회원 처리 시 사용자 조회 실패 오류 테스트")
        void shouldHandleUserNotFoundError() throws IOException {
            // given
            // 1. 기존 회원 정보를 설정합니다.
            Map<String, Object> attributes = Map.of(
                "isExistingUser", true,
                "provider", TEST_PROVIDER,
                "userId", TEST_USER_ID,
                "userRole", TEST_USER_ROLE
            );
            given(oAuth2User.getAttributes()).willReturn(attributes);

            // 2. 사용자 조회가 실패하도록 설정합니다.
            given(userRepository.findByIdWithAuth(TEST_USER_ID)).willReturn(Optional.empty());

            // when
            // 1. OAuth2 로그인 성공 처리를 실행합니다.
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            // 1. 사용자 조회는 수행되었는지 확인합니다.
            then(userRepository).should(times(1)).findByIdWithAuth(TEST_USER_ID);

            // 2. 오류 처리로 에러 페이지로 리다이렉트되는지 확인합니다.
            // URLEncoder.encode()의 인코딩 결과를 고려하여 부분적으로 확인합니다.
            then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("/login"));
            then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("error=oauth2_error"));

            // 3. JWT 토큰 생성은 수행되지 않았는지 확인합니다.
            then(jwtConfig).should(times(0)).generateAccessToken(anyString(), anyInt());
        }
    }

    @Nested
    @DisplayName("신규 회원 처리 테스트")
    class NewUserTest {

        @Test
        @DisplayName("신규 회원 OAuth2 로그인 성공 테스트")
        void shouldHandleNewUserSuccessfully() throws IOException {
            // given
            // 1. OAuth2 사용자 정보에 신규 회원 정보를 설정합니다.
            Map<String, Object> attributes = Map.of(
                "isExistingUser", false,
                "provider", TEST_PROVIDER
            );
            given(oAuth2User.getAttributes()).willReturn(attributes);

            // 2. OAuth2UserInfoFactory Mock 설정 - 실제 구현에서 사용하는 패턴
            try (MockedStatic<OAuth2UserInfoFactory> factoryMock = mockStatic(OAuth2UserInfoFactory.class)) {
                // OAuth2UserInfo Mock 객체 설정
                given(oAuth2UserInfo.getId()).willReturn(TEST_SOCIAL_ID);
                given(oAuth2UserInfo.getEmail()).willReturn(TEST_EMAIL);
                given(oAuth2UserInfo.getName()).willReturn(TEST_NAME);

                // Factory에서 OAuth2UserInfo 반환하도록 설정
                factoryMock.when(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(eq(TEST_PROVIDER), any(Map.class)))
                          .thenReturn(oAuth2UserInfo);

                // when
                // 1. OAuth2 로그인 성공 처리를 실행합니다.
                successHandler.onAuthenticationSuccess(request, response, authentication);

                // then
                // 1. OAuth2UserInfoFactory가 호출되었는지 확인합니다.
                factoryMock.verify(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(eq(TEST_PROVIDER), any(Map.class)));

                // 2. URL 인코딩을 고려하여 부분적으로 확인합니다.
                then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("/social-signup"));
                then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("isNewUser=true"));
                then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("provider=" + TEST_PROVIDER));
                then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("success=true"));
            }
        }

        @Test
        @DisplayName("신규 회원 처리 시 OAuth2UserInfo 생성 실패 테스트")
        void shouldHandleOAuth2UserInfoCreationError() throws IOException {
            // given
            // 1. 신규 회원 정보를 설정하지만 지원하지 않는 제공자를 사용합니다.
            String unsupportedProvider = "unsupported_provider";
            Map<String, Object> attributes = Map.of(
                "isExistingUser", false,
                "provider", unsupportedProvider
            );
            given(oAuth2User.getAttributes()).willReturn(attributes);

            // when & then
            // 1. OAuth2 로그인 성공 처리를 실행합니다. (실제 Factory에서 예외 발생)
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // 2. 오류 처리로 에러 페이지로 리다이렉트되는지 확인합니다.
            // URLEncoder.encode()의 인코딩 방식을 고려하여 부분적으로 확인합니다.
            then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("/login"));
            then(redirectStrategy).should(times(1)).sendRedirect(eq(request), eq(response), contains("error=oauth2_error"));
        }
    }

    @Nested
    @DisplayName("클라이언트 IP 추출 테스트")
    class ClientIpExtractionTest {

        @Test
        @DisplayName("X-Forwarded-For 헤더에서 클라이언트 IP 추출 성공 테스트")
        void shouldExtractClientIpFromXForwardedForHeader() throws IOException {
            // given
            // 1. X-Forwarded-For 헤더를 설정합니다.
            String forwardedIp = "192.168.1.100";
            given(request.getHeader("X-Forwarded-For")).willReturn(forwardedIp + ", 10.0.0.1");

            // 2. 기존 회원 OAuth2 정보를 설정합니다.
            Map<String, Object> attributes = Map.of(
                "isExistingUser", true,
                "provider", TEST_PROVIDER,
                "userId", TEST_USER_ID,
                "userRole", TEST_USER_ROLE
            );
            given(oAuth2User.getAttributes()).willReturn(attributes);

            User existingUser = User.create(TEST_NAME, TEST_EMAIL, "010-1234-5678");
            ReflectionTestUtils.setField(existingUser, "userId", TEST_USER_ID);
            given(userRepository.findByIdWithAuth(TEST_USER_ID)).willReturn(Optional.of(existingUser));
            given(jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID)).willReturn(TEST_ACCESS_TOKEN);
            given(tokenService.createRefreshToken(existingUser, forwardedIp)).willReturn(TEST_REFRESH_TOKEN);

            // when
            // 1. OAuth2 로그인 성공 처리를 실행합니다.
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            // 1. X-Forwarded-For 헤더의 첫 번째 IP가 사용되었는지 확인합니다.
            then(tokenService).should(times(1)).createRefreshToken(existingUser, forwardedIp);
        }

        @Test
        @DisplayName("X-Real-IP 헤더에서 클라이언트 IP 추출 성공 테스트")
        void shouldExtractClientIpFromXRealIpHeader() throws IOException {
            // given
            // 1. X-Real-IP 헤더를 설정합니다. (X-Forwarded-For는 없음)
            String realIp = "192.168.1.200";
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getHeader("X-Real-IP")).willReturn(realIp);

            // 2. 기존 회원 OAuth2 정보를 설정합니다.
            Map<String, Object> attributes = Map.of(
                "isExistingUser", true,
                "provider", TEST_PROVIDER,
                "userId", TEST_USER_ID,
                "userRole", TEST_USER_ROLE
            );
            given(oAuth2User.getAttributes()).willReturn(attributes);

            User existingUser = User.create(TEST_NAME, TEST_EMAIL, "010-1234-5678");
            ReflectionTestUtils.setField(existingUser, "userId", TEST_USER_ID);
            given(userRepository.findByIdWithAuth(TEST_USER_ID)).willReturn(Optional.of(existingUser));
            given(jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID)).willReturn(TEST_ACCESS_TOKEN);
            given(tokenService.createRefreshToken(existingUser, realIp)).willReturn(TEST_REFRESH_TOKEN);

            // when
            // 1. OAuth2 로그인 성공 처리를 실행합니다.
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            // 1. X-Real-IP 헤더의 IP가 사용되었는지 확인합니다.
            then(tokenService).should(times(1)).createRefreshToken(existingUser, realIp);
        }

        @Test
        @DisplayName("RemoteAddr에서 클라이언트 IP 추출 성공 테스트")
        void shouldExtractClientIpFromRemoteAddr() throws IOException {
            // given
            // 1. 특수 헤더들이 없는 경우를 설정합니다.
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getHeader("X-Real-IP")).willReturn(null);
            given(request.getRemoteAddr()).willReturn(TEST_CLIENT_IP);

            // 2. 기존 회원 OAuth2 정보를 설정합니다.
            Map<String, Object> attributes = Map.of(
                "isExistingUser", true,
                "provider", TEST_PROVIDER,
                "userId", TEST_USER_ID,
                "userRole", TEST_USER_ROLE
            );
            given(oAuth2User.getAttributes()).willReturn(attributes);

            User existingUser = User.create(TEST_NAME, TEST_EMAIL, "010-1234-5678");
            ReflectionTestUtils.setField(existingUser, "userId", TEST_USER_ID);
            given(userRepository.findByIdWithAuth(TEST_USER_ID)).willReturn(Optional.of(existingUser));
            given(jwtConfig.generateAccessToken(TEST_EMAIL, TEST_USER_ID)).willReturn(TEST_ACCESS_TOKEN);
            given(tokenService.createRefreshToken(existingUser, TEST_CLIENT_IP)).willReturn(TEST_REFRESH_TOKEN);

            // when
            // 1. OAuth2 로그인 성공 처리를 실행합니다.
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            // 1. RemoteAddr의 IP가 사용되었는지 확인합니다.
            then(tokenService).should(times(1)).createRefreshToken(existingUser, TEST_CLIENT_IP);
        }
    }
}