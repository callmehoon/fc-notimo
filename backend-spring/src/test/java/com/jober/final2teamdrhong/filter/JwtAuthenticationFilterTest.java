package com.jober.final2teamdrhong.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.config.JwtConfig;
import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.exception.ErrorResponse;
import com.jober.final2teamdrhong.service.BlacklistService;
import com.jober.final2teamdrhong.service.JwtClaimsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * JwtAuthenticationFilter 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private JwtClaimsService jwtClaimsService;

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 테스트 상수
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_EMAIL = "test@example.com";
    private static final Integer TEST_USER_ID = 1;
    private static final String TEST_USER_NAME = "테스트사용자";
    private static final String BEARER_TOKEN = "Bearer " + TEST_TOKEN;
    private static final String PRIVATE_URI = "/api/user/profile";
    private static final String PUBLIC_AUTH_URI = "/api/auth/login";
    private static final String PUBLIC_SWAGGER_URI = "/api/swagger-ui/index.html";

    @BeforeEach
    void setUp() {
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("공개 엔드포인트 처리 테스트")
    class PublicEndpointTest {

        @Test
        @DisplayName("인증 API 엔드포인트는 익명 인증으로 처리")
        void doFilterInternal_AuthEndpoint_SetsAnonymousAuthentication() throws Exception {
            // given
            given(request.getRequestURI()).willReturn(PUBLIC_AUTH_URI);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getName()).isEqualTo("anonymous");
            assertThat(auth.getAuthorities()).isEmpty();

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtConfig, jwtClaimsService, blacklistService);
        }

        @Test
        @DisplayName("Swagger UI 엔드포인트는 익명 인증으로 처리")
        void doFilterInternal_SwaggerEndpoint_SetsAnonymousAuthentication() throws Exception {
            // given
            given(request.getRequestURI()).willReturn(PUBLIC_SWAGGER_URI);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getName()).isEqualTo("anonymous");

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("API Docs 엔드포인트는 공개 처리")
        void doFilterInternal_ApiDocsEndpoint_IsPublic() throws Exception {
            // given
            given(request.getRequestURI()).willReturn("/api/v3/api-docs");

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("기존 인증 확인 테스트")
    class ExistingAuthenticationTest {

        @Test
        @DisplayName("이미 인증된 경우 필터를 건너뜀")
        void doFilterInternal_AlreadyAuthenticated_SkipsFilter() throws Exception {
            // given
            given(request.getRequestURI()).willReturn(PRIVATE_URI);

            // 기존 인증 설정
            Authentication existingAuth = mock(Authentication.class);
            given(existingAuth.isAuthenticated()).willReturn(true);
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtConfig, jwtClaimsService, blacklistService);
        }
    }

    @Nested
    @DisplayName("토큰 추출 및 검증 테스트")
    class TokenExtractionValidationTest {

        @BeforeEach
        void setUpPrivateEndpoint() {
            given(request.getRequestURI()).willReturn(PRIVATE_URI);
        }

        @Test
        @DisplayName("Authorization 헤더가 없는 경우 401 에러")
        void doFilterInternal_NoAuthorizationHeader_Returns401() throws Exception {
            // given
            given(request.getHeader("Authorization")).willReturn(null);
            given(jwtConfig.extractTokenFromHeader(null)).willReturn(null);
            setupErrorResponse("인증 토큰이 제공되지 않았습니다.");

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setCharacterEncoding("UTF-8");
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("토큰이 블랙리스트에 있는 경우 401 에러")
        void doFilterInternal_TokenBlacklisted_Returns401() throws Exception {
            // given
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(jwtConfig.extractTokenFromHeader(BEARER_TOKEN)).willReturn(TEST_TOKEN);
            given(blacklistService.isTokenBlacklisted(TEST_TOKEN)).willReturn(true);
            setupErrorResponse("무효화된 토큰입니다. 다시 로그인해주세요.");

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
            verify(blacklistService).isTokenBlacklisted(TEST_TOKEN);
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("토큰 검증 실패 시 401 에러")
        void doFilterInternal_TokenValidationFails_Returns401() throws Exception {
            // given
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(jwtConfig.extractTokenFromHeader(BEARER_TOKEN)).willReturn(TEST_TOKEN);
            given(blacklistService.isTokenBlacklisted(TEST_TOKEN)).willReturn(false);
            given(jwtConfig.validateToken(TEST_TOKEN)).willReturn(false);
            setupErrorResponseForTokenValidation();

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(jwtConfig).validateToken(TEST_TOKEN);
            verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
            verify(filterChain, never()).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("성공적인 인증 처리 테스트")
    class SuccessfulAuthenticationTest {

        private JwtClaims testClaims;

        @BeforeEach
        void setUpSuccessfulAuthentication() {
            given(request.getRequestURI()).willReturn(PRIVATE_URI);
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(jwtConfig.extractTokenFromHeader(BEARER_TOKEN)).willReturn(TEST_TOKEN);
            given(blacklistService.isTokenBlacklisted(TEST_TOKEN)).willReturn(false);
            given(jwtConfig.validateToken(TEST_TOKEN)).willReturn(true);

            testClaims = JwtClaims.builder()
                    .email(TEST_EMAIL)
                    .userId(TEST_USER_ID)
                    .userName(TEST_USER_NAME)
                    .userRole(User.UserRole.USER)
                    .tokenType("access")
                    .jti("test-jti")
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();
        }

        @Test
        @DisplayName("유효한 토큰으로 인증 성공")
        void doFilterInternal_ValidToken_SetsAuthentication() throws Exception {
            // given
            given(jwtClaimsService.getEnrichedClaims(TEST_TOKEN)).willReturn(testClaims);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getPrincipal()).isEqualTo(testClaims);
            assertThat(auth.getCredentials()).isNull();
            assertThat(auth.getAuthorities())
                    .hasSize(1)
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_USER");
            assertThat(auth.isAuthenticated()).isTrue();

            verify(jwtClaimsService).getEnrichedClaims(TEST_TOKEN);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("ADMIN 권한 사용자 인증 성공")
        void doFilterInternal_AdminUser_SetsAdminAuthentication() throws Exception {
            // given
            JwtClaims adminClaims = JwtClaims.builder()
                    .email("admin@example.com")
                    .userId(2)
                    .userName("관리자")
                    .userRole(User.UserRole.ADMIN)
                    .tokenType("access")
                    .jti("admin-jti")
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();

            given(jwtClaimsService.getEnrichedClaims(TEST_TOKEN)).willReturn(adminClaims);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getPrincipal()).isEqualTo(adminClaims);
            assertThat(auth.getAuthorities())
                    .hasSize(1)
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_ADMIN");

            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("JWT Claims 서비스 예외 처리 테스트")
    class JwtClaimsServiceExceptionTest {

        @BeforeEach
        void setUpValidToken() {
            given(request.getRequestURI()).willReturn(PRIVATE_URI);
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(jwtConfig.extractTokenFromHeader(BEARER_TOKEN)).willReturn(TEST_TOKEN);
            given(blacklistService.isTokenBlacklisted(TEST_TOKEN)).willReturn(false);
            given(jwtConfig.validateToken(TEST_TOKEN)).willReturn(true);
        }

        @Test
        @DisplayName("Claims 서비스에서 예외 발생 시 401 에러")
        void doFilterInternal_JwtClaimsServiceException_Returns401() throws Exception {
            // given
            RuntimeException exception = new RuntimeException("사용자를 찾을 수 없습니다");
            given(jwtClaimsService.getEnrichedClaims(TEST_TOKEN)).willThrow(exception);
            setupErrorResponseForException(exception);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
            verify(filterChain, never()).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("에러 메시지 판단 테스트")
    class ErrorMessageDeterminationTest {

        @Test
        @DisplayName("Authorization 헤더가 없는 경우 적절한 에러 메시지")
        void determineJwtErrorMessage_NoHeader_ReturnsAppropriateMessage() throws Exception {
            // given
            given(request.getRequestURI()).willReturn(PRIVATE_URI);
            given(request.getHeader("Authorization")).willReturn(null);
            given(jwtConfig.extractTokenFromHeader(null)).willReturn(null);
            setupErrorResponse("인증 토큰이 제공되지 않았습니다.");

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("Bearer 접두사가 없는 경우 적절한 에러 메시지")
        void determineJwtErrorMessage_NoBearerPrefix_ReturnsAppropriateMessage() throws Exception {
            // given
            String invalidHeader = "InvalidToken";
            given(request.getRequestURI()).willReturn(PRIVATE_URI);
            given(request.getHeader("Authorization")).willReturn(invalidHeader);
            given(jwtConfig.extractTokenFromHeader(invalidHeader)).willReturn(null);
            setupErrorResponse("인증 토큰이 제공되지 않았습니다.");

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    // === Helper Methods ===

    /**
     * 에러 응답 설정을 위한 헬퍼 메서드
     */
    private void setupErrorResponse(String errorMessage) throws Exception {
        given(response.getWriter()).willReturn(writer);
        String jsonResponse = "{\"message\":\"" + errorMessage + "\"}";
        given(objectMapper.writeValueAsString(any(ErrorResponse.class))).willReturn(jsonResponse);
    }

    /**
     * 토큰 검증 실패 시 에러 응답 설정
     */
    private void setupErrorResponseForTokenValidation() throws Exception {
        given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
        setupErrorResponse("인증에 실패했습니다. 유효한 토큰을 제공해주세요.");
    }

    /**
     * 예외 발생 시 에러 응답 설정
     */
    private void setupErrorResponseForException(Exception exception) throws Exception {
        setupErrorResponse("인증에 실패했습니다. 유효한 토큰을 제공해주세요.");
    }

    @Nested
    @DisplayName("공개 엔드포인트 판단 로직 테스트")
    class PublicEndpointDetectionTest {

        @Test
        @DisplayName("다양한 공개 엔드포인트 패턴 확인")
        void isPublicEndpoint_VariousPatterns_ReturnsCorrectly() throws Exception {
            String[] publicEndpoints = {
                "/api/auth/login",
                "/api/auth/signup",
                "/api/auth/refresh",
                "/api/swagger-ui/index.html",
                "/api/v3/api-docs/swagger-config",
                "/api/swagger-resources/configuration/ui",
                "/api/webjars/swagger-ui/index.html"
            };

            for (String endpoint : publicEndpoints) {
                // given
                given(request.getRequestURI()).willReturn(endpoint);
                SecurityContextHolder.clearContext(); // 각 테스트마다 초기화

                // when
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // then
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                assertThat(auth).isNotNull();
                assertThat(auth.getName()).isEqualTo("anonymous");
            }

            // 공개 엔드포인트 개수만큼 filterChain.doFilter가 호출되었는지 확인
            verify(filterChain, times(publicEndpoints.length)).doFilter(request, response);
        }

        @Test
        @DisplayName("비공개 엔드포인트는 JWT 인증 처리")
        void isPublicEndpoint_PrivateEndpoint_RequiresAuthentication() throws Exception {
            String[] privateEndpoints = {
                "/api/user/profile",
                "/api/workspace/list",
                "/api/template/create",
                "/api/admin/users"
            };

            for (String endpoint : privateEndpoints) {
                // given
                given(request.getRequestURI()).willReturn(endpoint);
                given(request.getHeader("Authorization")).willReturn(null);
                given(jwtConfig.extractTokenFromHeader(null)).willReturn(null);
                setupErrorResponse("인증 토큰이 제공되지 않았습니다.");
                SecurityContextHolder.clearContext(); // 각 테스트마다 초기화

                // when
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // then - JWT 인증 로직이 실행되어 401 에러가 발생해야 함
                verify(response, atLeastOnce()).setStatus(HttpStatus.UNAUTHORIZED.value());
            }
        }
    }
}