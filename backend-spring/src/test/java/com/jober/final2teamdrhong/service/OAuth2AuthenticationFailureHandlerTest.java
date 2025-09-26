package com.jober.final2teamdrhong.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * OAuth2AuthenticationFailureHandler 테스트
 * 실제 구현에 맞춘 포괄적인 테스트 커버리지
 */
@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    private OAuth2AuthenticationFailureHandler failureHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private DefaultRedirectStrategy redirectStrategy;

    private static final String TEST_REDIRECT_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        // 실제 OAuth2AuthenticationFailureHandler 인스턴스 생성
        failureHandler = new OAuth2AuthenticationFailureHandler();

        // @Value로 주입되는 allowedOrigins 설정
        ReflectionTestUtils.setField(failureHandler, "allowedOrigins", TEST_REDIRECT_URL);

        // Mock redirectStrategy 주입 (실제로는 상속받은 getRedirectStrategy()를 사용)
        ReflectionTestUtils.setField(failureHandler, "redirectStrategy", redirectStrategy);

        // HttpServletRequest mock 기본 설정
        when(request.getRequestURI()).thenReturn("/oauth2/authorization/google");
    }

    @Nested
    @DisplayName("OAuth2 표준 에러 코드 처리 테스트")
    class StandardOAuth2ErrorTest {

        @Test
        @DisplayName("access_denied 에러 처리 - 사용자 권한 승인 취소")
        void shouldHandleAccessDeniedError() throws IOException {
            // given - access_denied 에러 코드가 포함된 메시지를 가진 예외
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("access_denied", "access_denied: The user denied the request", null)
            );

            // when - OAuth2 로그인 실패 처리 실행
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then - 사용자 친화적 메시지와 함께 리다이렉트 확인
            String expectedUrl = TEST_REDIRECT_URL + "/login" +
                "?error=oauth2_failure" +
                "&message=%EC%82%AC%EC%9A%A9%EC%9E%90%EA%B0%80+%EA%B6%8C%ED%95%9C+%EC%8A%B9%EC%9D%B8%EC%9D%84+%EC%B7%A8%EC%86%8C%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4.";
            then(redirectStrategy).should(times(1)).sendRedirect(request, response, expectedUrl);
        }

        @Test
        @DisplayName("invalid_request 에러 처리 - 잘못된 요청")
        void shouldHandleInvalidRequestError() throws IOException {
            // given - invalid_request 에러 코드가 포함된 메시지를 가진 예외
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("invalid_request", "invalid_request: The request is missing a parameter", null)
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            String expectedUrl = TEST_REDIRECT_URL + "/login" +
                "?error=oauth2_failure" +
                "&message=%EC%9E%98%EB%AA%BB%EB%90%9C+%EC%9A%94%EC%B2%AD%EC%9E%85%EB%8B%88%EB%8B%A4.+%EB%8B%A4%EC%8B%9C+%EC%8B%9C%EB%8F%84%ED%95%B4+%EC%A3%BC%EC%84%B8%EC%9A%94.";
            then(redirectStrategy).should(times(1)).sendRedirect(request, response, expectedUrl);
        }

        @Test
        @DisplayName("server_error 에러 처리 - 서버 오류")
        void shouldHandleServerError() throws IOException {
            // given - server_error 에러 코드가 포함된 메시지를 가진 예외
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("server_error", "server_error: The authorization server encountered an error", null)
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            String expectedUrl = TEST_REDIRECT_URL + "/login" +
                "?error=oauth2_failure" +
                "&message=%EC%84%9C%EB%B2%84+%EC%98%A4%EB%A5%98%EA%B0%80+%EB%B0%9C%EC%83%9D%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4.+%EC%9E%A0%EC%8B%9C+%ED%9B%84+%EB%8B%A4%EC%8B%9C+%EC%8B%9C%EB%8F%84%ED%95%B4+%EC%A3%BC%EC%84%B8%EC%9A%94.";
            then(redirectStrategy).should(times(1)).sendRedirect(request, response, expectedUrl);
        }

        @Test
        @DisplayName("unauthorized_client 에러 처리")
        void shouldHandleUnauthorizedClientError() throws IOException {
            // given - unauthorized_client 에러 코드가 포함된 메시지를 가진 예외
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("unauthorized_client", "unauthorized_client: Client authentication failed", null)
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            String expectedUrl = TEST_REDIRECT_URL + "/login" +
                "?error=oauth2_failure" +
                "&message=%EC%9D%B8%EC%A6%9D%EB%90%98%EC%A7%80+%EC%95%8A%EC%9D%80+%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8%EC%9E%85%EB%8B%88%EB%8B%A4.";
            then(redirectStrategy).should(times(1)).sendRedirect(request, response, expectedUrl);
        }

        @Test
        @DisplayName("temporarily_unavailable 에러 처리")
        void shouldHandleTemporarilyUnavailableError() throws IOException {
            // given - temporarily_unavailable 에러 코드가 포함된 메시지를 가진 예외
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("temporarily_unavailable", "temporarily_unavailable: Service temporarily unavailable", null)
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then
            String expectedUrl = TEST_REDIRECT_URL + "/login" +
                "?error=oauth2_failure" +
                "&message=%EC%84%9C%EB%B9%84%EC%8A%A4%EA%B0%80+%EC%9D%BC%EC%8B%9C%EC%A0%81%EC%9C%BC%EB%A1%9C+%EC%82%AC%EC%9A%A9%ED%95%A0+%EC%88%98+%EC%97%86%EC%8A%B5%EB%8B%88%EB%8B%A4.+%EC%9E%A0%EC%8B%9C+%ED%9B%84+%EB%8B%A4%EC%8B%9C+%EC%8B%9C%EB%8F%84%ED%95%B4+%EC%A3%BC%EC%84%B8%EC%9A%94.";
            then(redirectStrategy).should(times(1)).sendRedirect(request, response, expectedUrl);
        }
    }

    @Nested
    @DisplayName("예외 클래스명 기반 처리 테스트")
    class ExceptionClassificationTest {

        @Test
        @DisplayName("OAuth2AuthenticationException 기본 처리")
        void shouldHandleOAuth2AuthenticationException() throws IOException {
            // given - 알려지지 않은 OAuth2 인증 예외
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("unknown_error", "Unknown error occurred", null)
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then - determineErrorMessage에서 클래스명 기반으로 처리됨
            String expectedUrl = TEST_REDIRECT_URL + "/login" +
                "?error=oauth2_failure" +
                "&message=%EC%86%8C%EC%85%9C+%EB%A1%9C%EA%B7%B8%EC%9D%B8+%EC%9D%B8%EC%A6%9D%EC%97%90+%EC%8B%A4%ED%8C%A8%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4.";
            then(redirectStrategy).should(times(1)).sendRedirect(request, response, expectedUrl);
        }

        @Test
        @DisplayName("알 수 없는 예외 기본 처리")
        void shouldHandleUnknownException() throws IOException {
            // given - 알 수 없는 유형의 인증 예외
            AuthenticationException exception = new AuthenticationException("Unknown authentication error") {};

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then - default case 처리
            String expectedUrl = TEST_REDIRECT_URL + "/login" +
                "?error=oauth2_failure" +
                "&message=%EC%86%8C%EC%85%9C+%EB%A1%9C%EA%B7%B8%EC%9D%B8+%EC%A4%91+%EC%98%A4%EB%A5%98%EA%B0%80+%EB%B0%9C%EC%83%9D%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4.+%EB%8B%A4%EC%8B%9C+%EC%8B%9C%EB%8F%84%ED%95%B4+%EC%A3%BC%EC%84%B8%EC%9A%94.";
            then(redirectStrategy).should(times(1)).sendRedirect(request, response, expectedUrl);
        }
    }

    @Nested
    @DisplayName("예외 처리 및 Fallback 로직 테스트")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("핸들러 처리 중 IOException 발생 시 fallback 처리")
        void shouldHandleFallbackWhenIOExceptionOccurs() throws IOException {
            // given - 첫 번째 리다이렉트에서 IOException 발생
            AuthenticationException authException = new OAuth2AuthenticationException(
                new OAuth2Error("test_error", "Test error", null)
            );

            // 첫 번째 sendRedirect 호출에서 IOException 발생하도록 설정
            doThrow(new IOException("Redirect failed"))
                .doNothing() // 두 번째 호출은 성공
                .when(redirectStrategy).sendRedirect(any(), any(), anyString());

            // when
            failureHandler.onAuthenticationFailure(request, response, authException);

            // then - 총 2번의 sendRedirect 호출 (첫 번째 실패 후 fallback)
            then(redirectStrategy).should(times(2)).sendRedirect(eq(request), eq(response), anyString());

            // fallback URL이 호출되었는지 확인
            then(redirectStrategy).should().sendRedirect(eq(request), eq(response),
                contains("error=system_error"));
        }
    }

    @Nested
    @DisplayName("URL 구성 및 인코딩 테스트")
    class UrlHandlingTest {

        @Test
        @DisplayName("여러 allowed origins가 있을 때 첫 번째 origin 사용")
        void shouldUseFirstAllowedOrigin() throws IOException {
            // given - 여러 개의 allowed origins 설정
            ReflectionTestUtils.setField(failureHandler, "allowedOrigins",
                "http://localhost:3000,http://localhost:8080,https://prod.example.com");

            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("test_error", "Test error", null)
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then - 첫 번째 origin이 사용됨
            then(redirectStrategy).should().sendRedirect(eq(request), eq(response),
                contains("http://localhost:3000/login"));
        }

        @Test
        @DisplayName("에러 메시지 URL 인코딩 확인")
        void shouldEncodeErrorMessage() throws IOException {
            // given - access_denied 에러 코드가 포함된 메시지
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("access_denied", "access_denied: User denied access", null)
            );

            // when
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then - 한국어 메시지가 URL 인코딩되어 전달됨
            then(redirectStrategy).should().sendRedirect(eq(request), eq(response),
                contains("%EC%82%AC%EC%9A%A9%EC%9E%90%EA%B0%80")); // "사용자가"의 인코딩 부분
        }
    }

    @Nested
    @DisplayName("로깅 및 요청 정보 처리 테스트")
    class LoggingTest {

        @Test
        @DisplayName("요청 URI 정보가 로깅에 포함되는지 확인")
        void shouldLogRequestURI() throws IOException {
            // given
            when(request.getRequestURI()).thenReturn("/oauth2/authorization/google");
            AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("test_error", "Test for logging", null)
            );

            // when - 로깅은 실제로는 확인하기 어려우므로 정상 처리만 확인
            failureHandler.onAuthenticationFailure(request, response, exception);

            // then - 정상적으로 리다이렉트가 호출되었는지 확인
            then(redirectStrategy).should().sendRedirect(any(), any(), anyString());
        }
    }
}