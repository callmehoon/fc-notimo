package com.jober.final2teamdrhong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.auth.SocialSignupRequest;
import com.jober.final2teamdrhong.dto.auth.SocialSignupResponse;
import com.jober.final2teamdrhong.exception.BusinessException;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SocialAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @MockitoBean
    private AuthService authService;

    // 테스트 상수 정의
    private static final String SOCIAL_AUTH_BASE_URL = "/auth/social";
    private static final String TEST_EMAIL = "social-test@example.com";
    private static final String TEST_NAME = "소셜테스트유저";
    private static final String TEST_PHONE = "010-9999-8888";
    private static final String TEST_SOCIAL_ID = "google_123456789";
    private static final String TEST_PROVIDER = "google";

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 초기화
        userRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN users_id RESTART WITH 1");
    }

    // ==================== 구글 소셜 로그인 테스트 ====================

    @Test
    @DisplayName("구글 로그인 시작 - 정상 리다이렉트")
    @WithAnonymousUser
    void shouldRedirectToGoogleOAuth2() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get(SOCIAL_AUTH_BASE_URL + "/login/google"));

        // then
        result.andDo(print())
                .andExpect(status().isFound()) // 302 Found
                .andExpect(header().string("Location", "/api/oauth2/authorization/google"));
    }

    // ==================== 소셜 제공자 목록 조회 테스트 ====================

    @Test
    @DisplayName("지원하는 소셜 제공자 목록 조회 - 성공")
    @WithAnonymousUser
    void shouldReturnSupportedProviders() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                get(SOCIAL_AUTH_BASE_URL + "/providers")
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.providers").exists())
                .andExpect(jsonPath("$.providers.google").exists())
                .andExpect(jsonPath("$.message").value(containsString("현재 지원하는 소셜 로그인 제공자 목록입니다")));
    }

    // ==================== 소셜 회원가입 완료 테스트 ====================

    @Test
    @DisplayName("소셜 회원가입 완료 - 성공")
    @WithAnonymousUser
    void shouldCompleteSocialSignupSuccessfully() throws Exception {
        // given
        SocialSignupRequest request = new SocialSignupRequest(
                TEST_PROVIDER,
                TEST_SOCIAL_ID,
                TEST_EMAIL,
                TEST_NAME,
                TEST_PHONE,
                true,  // agreedToTerms
                true,  // agreedToPrivacyPolicy
                false  // agreedToMarketing
        );

        SocialSignupResponse expectedResponse = SocialSignupResponse.success(
                1,  // userId
                "mock_access_token",
                "mock_refresh_token",
                TEST_EMAIL,
                TEST_NAME,
                "USER",
                TEST_PROVIDER
        );

        given(authService.completeSocialSignup(any(SocialSignupRequest.class)))
                .willReturn(expectedResponse);

        // when
        ResultActions result = mockMvc.perform(
                post(SOCIAL_AUTH_BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.name").value(TEST_NAME))
                .andExpect(jsonPath("$.userRole").value("USER"))
                .andExpect(jsonPath("$.provider").value(TEST_PROVIDER));
    }

    @Test
    @DisplayName("소셜 회원가입 완료 - 이미 존재하는 사용자 (409 Conflict)")
    @WithAnonymousUser
    void shouldFailWithExistingUser() throws Exception {
        // given
        SocialSignupRequest request = new SocialSignupRequest(
                TEST_PROVIDER,
                TEST_SOCIAL_ID,
                TEST_EMAIL,
                TEST_NAME,
                TEST_PHONE,
                true,  // agreedToTerms
                true,  // agreedToPrivacyPolicy
                false  // agreedToMarketing
        );

        // AuthService가 중복 사용자 예외를 던지도록 설정
        given(authService.completeSocialSignup(any(SocialSignupRequest.class)))
                .willThrow(new BusinessException("이미 존재하는 사용자입니다."));

        // when
        ResultActions result = mockMvc.perform(
                post(SOCIAL_AUTH_BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 사용자입니다."));
    }
}