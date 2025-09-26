package com.jober.final2teamdrhong.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 로그인 실패 시 처리하는 핸들러.
 * 에러 정보를 포함하여 프론트엔드 로그인 페이지로 리다이렉트합니다.
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String allowedOrigin;

    // 생성자를 통한 @Value 주입 및 final 필드 사용
    public OAuth2AuthenticationFailureHandler(@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}") String allowedOrigins) {
        // 여러 오리진 중 첫 번째를 기본 리다이렉트 대상으로 설정
        this.allowedOrigin = allowedOrigins.split(",")[0].trim();
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String targetUrl = "";
        try {
            String errorMessage = determineErrorMessage(exception);

            log.error("OAuth2 로그인 실패 - 요청 URI: {}, 오류: {}",
                    request.getRequestURI(), exception.getMessage(), exception);

            // 1. 에러 정보와 함께 프론트엔드 로그인 페이지로 리다이렉트 URL 구성
            targetUrl = UriComponentsBuilder.fromUriString(allowedOrigin)
                    .path("/login")
                    .queryParam("error", "oauth2_failure")
                    // 에러 메시지는 URL 인코딩 필수
                    .queryParam("message", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                    .build().toUriString();

        } catch (Exception e) {
            log.error("OAuth2 실패 핸들러 처리 중 오류 발생: {}", e.getMessage(), e);

            // 2. 오류 발생 시, 시스템 에러를 알리는 대체 URL 구성
            targetUrl = UriComponentsBuilder.fromUriString(allowedOrigin)
                    .path("/login")
                    .queryParam("error", "system_error")
                    .queryParam("message", URLEncoder.encode("로그인 처리 중 예기치 않은 시스템 오류가 발생했습니다.", StandardCharsets.UTF_8))
                    .build().toUriString();
        }

        // 3. 구성된 URL로 최종 리다이렉트 실행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * AuthenticationException을 분석하여 사용자 친화적인 에러 메시지를 반환합니다.
     * @param exception 인증 실패 예외
     * @return 사용자에게 보여줄 에러 메시지
     */
    private String determineErrorMessage(AuthenticationException exception) {
        // OAuth2 관련 예외 처리를 최우선으로 합니다.
        if (exception instanceof OAuth2AuthenticationException oAuth2Exception) {
            OAuth2Error error = oAuth2Exception.getError();
            String errorCode = error.getErrorCode();

            return switch (errorCode) {
                case OAuth2ErrorCodes.ACCESS_DENIED -> "사용자가 소셜 로그인 권한 승인을 거부했습니다.";
                case OAuth2ErrorCodes.INVALID_REQUEST -> "소셜 로그인 요청 정보가 잘못되었습니다. 다시 시도해 주세요.";
                case OAuth2ErrorCodes.UNAUTHORIZED_CLIENT -> "인증되지 않은 소셜 로그인 클라이언트입니다.";
                case OAuth2ErrorCodes.INVALID_SCOPE -> "요청한 권한 범위(Scope)가 유효하지 않습니다.";
                // Spring Security 표준 에러 코드 중 일반적인 서버 에러
                case OAuth2ErrorCodes.SERVER_ERROR, OAuth2ErrorCodes.TEMPORARILY_UNAVAILABLE -> "소셜 서비스 서버에서 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
                default -> {
                    log.warn("처리되지 않은 OAuth2 에러 코드: {} - {}", errorCode, error.getDescription());
                    yield "소셜 로그인 인증에 실패했습니다.";
                }
            };
        }

        // 기타 Spring Security 또는 시스템 레벨 예외 처리
        String className = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage() != null ? exception.getMessage() : className;

        return switch (className) {
            case "BadCredentialsException" -> "인증 정보가 유효하지 않습니다."; // OAuth2에서는 흔치 않으나 포함
            case "InternalAuthenticationServiceException" -> "로그인 처리 중 내부 서비스 오류가 발생했습니다.";
            // 기타 예상치 못한 오류
            default -> {
                log.warn("처리되지 않은 인증 예외 유형: {} - {}", className, exceptionMessage);
                yield "소셜 로그인 중 예기치 않은 오류가 발생했습니다. 다시 시도해 주세요.";
            }
        };
    }
}