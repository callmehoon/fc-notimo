package com.jober.final2teamdrhong.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.RedirectStrategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2 인증 실패 핸들러 테스트")
class OAuth2AuthenticationFailureHandlerTest {

    private OAuth2AuthenticationFailureHandler handler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RedirectStrategy redirectStrategy;

    private final String allowedOrigin = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        // 리팩토링된 생성자로 핸들러 생성
        handler = new OAuth2AuthenticationFailureHandler(allowedOrigin);
        handler.setRedirectStrategy(redirectStrategy);

        // 요청 URI Mock 설정 (로깅에서만 사용)
        lenient().when(request.getRequestURI()).thenReturn("/oauth2/authorization/google");
    }

    @Test
    @DisplayName("권한 거부(Access Denied) 예외 발생 시, 올바른 메시지로 리다이렉트한다")
    void onAuthenticationFailure_withAccessDeniedException_shouldRedirectWithCorrectMessage() throws IOException {
        // Given
        OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED, "User denied access", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(oauth2Error);

        String expectedRedirectUrl = "http://localhost:3000/login?error=oauth2_failure&message=%EC%82%AC%EC%9A%A9%EC%9E%90%EA%B0%80+%EC%86%8C%EC%85%9C+%EB%A1%9C%EA%B7%B8%EC%9D%B8+%EA%B6%8C%ED%95%9C+%EC%8A%B9%EC%9D%B8%EC%9D%84+%EA%B1%B0%EB%B6%80%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4.";

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(redirectStrategy).sendRedirect(request, response, expectedRedirectUrl);
    }

    @Test
    @DisplayName("잘못된 요청(Invalid Request) 예외 발생 시, 올바른 메시지로 리다이렉트한다")
    void onAuthenticationFailure_withInvalidRequestException_shouldRedirectWithCorrectMessage() throws IOException {
        // Given
        OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "The request is missing a parameter", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(oauth2Error);

        String expectedRedirectUrl = "http://localhost:3000/login?error=oauth2_failure&message=%EC%86%8C%EC%85%9C+%EB%A1%9C%EA%B7%B8%EC%9D%B8+%EC%9A%94%EC%B2%AD+%EC%A0%95%EB%B3%B4%EA%B0%80+%EC%9E%98%EB%AA%BB%EB%90%98%EC%97%88%EC%8A%B5%EB%8B%88%EB%8B%A4.+%EB%8B%A4%EC%8B%9C+%EC%8B%9C%EB%8F%84%ED%95%B4+%EC%A3%BC%EC%84%B8%EC%9A%94.";

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(redirectStrategy).sendRedirect(request, response, expectedRedirectUrl);
    }

    @Test
    @DisplayName("서버 오류(Server Error) 예외 발생 시, 올바른 메시지로 리다이렉트한다")
    void onAuthenticationFailure_withServerErrorException_shouldRedirectWithCorrectMessage() throws IOException {
        // Given
        OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "The authorization server encountered an error", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(oauth2Error);

        String expectedRedirectUrl = "http://localhost:3000/login?error=oauth2_failure&message=%EC%86%8C%EC%85%9C+%EC%84%9C%EB%B9%84%EC%8A%A4+%EC%84%9C%EB%B2%84%EC%97%90%EC%84%9C+%EC%9D%BC%EC%8B%9C%EC%A0%81%EC%9D%B8+%EC%98%A4%EB%A5%98%EA%B0%80+%EB%B0%9C%EC%83%9D%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4.+%EC%9E%A0%EC%8B%9C+%ED%9B%84+%EB%8B%A4%EC%8B%9C+%EC%8B%9C%EB%8F%84%ED%95%B4+%EC%A3%BC%EC%84%B8%EC%9A%94.";

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(redirectStrategy).sendRedirect(request, response, expectedRedirectUrl);
    }

    @Test
    @DisplayName("무효한 클라이언트(Unauthorized Client) 예외 발생 시, 올바른 메시지로 리다이렉트한다")
    void onAuthenticationFailure_withUnauthorizedClientException_shouldRedirectWithCorrectMessage() throws IOException {
        // Given
        OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, "Client authentication failed", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(oauth2Error);

        String expectedRedirectUrl = "http://localhost:3000/login?error=oauth2_failure&message=%EC%9D%B8%EC%A6%9D%EB%90%98%EC%A7%80+%EC%95%8A%EC%9D%80+%EC%86%8C%EC%85%9C+%EB%A1%9C%EA%B7%B8%EC%9D%B8+%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8%EC%9E%85%EB%8B%88%EB%8B%A4.";

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(redirectStrategy).sendRedirect(request, response, expectedRedirectUrl);
    }

    @Test
    @DisplayName("알 수 없는 OAuth2 예외 발생 시, 기본 오류 메시지로 리다이렉트한다")
    void onAuthenticationFailure_withUnknownOAuth2Exception_shouldRedirectWithDefaultMessage() throws IOException {
        // Given
        OAuth2Error oauth2Error = new OAuth2Error("UNKNOWN_ERROR", "Unknown error occurred", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(oauth2Error);

        String expectedRedirectUrl = "http://localhost:3000/login?error=oauth2_failure&message=%EC%86%8C%EC%85%9C+%EB%A1%9C%EA%B7%B8%EC%9D%B8+%EC%9D%B8%EC%A6%9D%EC%97%90+%EC%8B%A4%ED%8C%A8%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4.";

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(redirectStrategy).sendRedirect(request, response, expectedRedirectUrl);
    }

    @Test
    @DisplayName("일반적인 AuthenticationException 발생 시, 시스템 오류 메시지로 리다이렉트한다")
    void onAuthenticationFailure_withGeneralException_shouldRedirectWithSystemErrorMessage() throws IOException {
        // Given
        AuthenticationException exception = new AuthenticationException("일반적인 인증 오류") {};

        String expectedRedirectUrl = "http://localhost:3000/login?error=oauth2_failure&message=%EC%86%8C%EC%85%9C+%EB%A1%9C%EA%B7%B8%EC%9D%B8+%EC%A4%91+%EC%98%88%EA%B8%B0%EC%B9%98+%EC%95%8A%EC%9D%80+%EC%98%A4%EB%A5%98%EA%B0%80+%EB%B0%9C%EC%83%9D%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4.+%EB%8B%A4%EC%8B%9C+%EC%8B%9C%EB%8F%84%ED%95%B4+%EC%A3%BC%EC%84%B8%EC%9A%94.";

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(redirectStrategy).sendRedirect(request, response, expectedRedirectUrl);
    }

    @Test
    @DisplayName("여러 allowed origins 설정 시 첫 번째 origin 사용")
    void onAuthenticationFailure_withMultipleOrigins_shouldUseFirstOrigin() throws IOException {
        // Given
        String multipleOrigins = "http://localhost:3000,http://localhost:8080,https://prod.example.com";
        OAuth2AuthenticationFailureHandler multiOriginHandler = new OAuth2AuthenticationFailureHandler(multipleOrigins);
        multiOriginHandler.setRedirectStrategy(redirectStrategy);

        OAuth2Error oauth2Error = new OAuth2Error("test_error", "Test error", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(oauth2Error);

        // When
        multiOriginHandler.onAuthenticationFailure(request, response, exception);

        // Then - 첫 번째 origin(localhost:3000)이 사용되어야 함
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), startsWith("http://localhost:3000/login"));
    }

    @Test
    @DisplayName("핸들러 처리 중 예외 발생 시, 시스템 에러로 처리")
    void onAuthenticationFailure_withHandlerException_shouldHandleAsSystemError() throws IOException {
        // Given - null exception으로 내부 처리 중 예외 발생 유도
        AuthenticationException nullException = null;

        String expectedSystemErrorUrl = "http://localhost:3000/login?error=system_error&message=%EB%A1%9C%EA%B7%B8%EC%9D%B8+%EC%B2%98%EB%A6%AC+%EC%A4%91+%EC%98%88%EA%B8%B0%EC%B9%98+%EC%95%8A%EC%9D%80+%EC%8B%9C%EC%8A%A4%ED%85%9C+%EC%98%A4%EB%A5%98%EA%B0%80+%EB%B0%9C%EC%83%9D%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4.";

        // When
        handler.onAuthenticationFailure(request, response, nullException);

        // Then - 시스템 에러로 처리되어야 함
        verify(redirectStrategy).sendRedirect(request, response, expectedSystemErrorUrl);
    }
}