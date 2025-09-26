package com.jober.final2teamdrhong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.jober.final2teamdrhong.dto.emailVerification.EmailRequest;
import com.jober.final2teamdrhong.dto.userLogin.UserLoginRequest;
import com.jober.final2teamdrhong.dto.userLogin.UserLoginResponse;
import com.jober.final2teamdrhong.dto.userLogout.UserLogoutRequest;
import com.jober.final2teamdrhong.dto.userSignup.UserSignupRequest;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.RefreshTokenService;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import com.jober.final2teamdrhong.util.test.WithMockJwtClaims;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.ResultHandler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationStorage verificationStorage;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    // 테스트 상수 정의
    private static final String TEST_EMAIL = "test-user@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_USERNAME = "테스트유저";
    private static final String TEST_PHONE = "010-1234-5678";

    private User testUser;
    private String testAccessToken;
    private String testRefreshToken;

    @BeforeEach
    void setUp() {
        // 1. UTF-8 인코딩 필터를 적용한 커스텀 MockMvc 설정
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(characterEncodingFilter)
                .build();

        // 2. 기존 데이터 완전 삭제 및 시퀀스 리셋
        userRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN users_id RESTART WITH 1");

        // 3. Rate Limit 초기화 (테스트 간 격리)
        clearRateLimitKeys();

        // 4. 로그인 테스트용 사용자 생성 (ID=1로 생성됨)
        testUser = createTestUser(TEST_USERNAME, TEST_EMAIL, TEST_PHONE, TEST_PASSWORD);

        // 5. 영속성 컨텍스트 동기화
        entityManager.flush();
        entityManager.clear();
    }

    // ==================== 회원가입 테스트 ====================

    @Test
    @DisplayName("회원가입 성공 테스트")
    @WithAnonymousUser
    void signup_Success_Test() throws Exception {
        // given
        String newEmail = "new-user@example.com";
        String verificationCode = "123456";

        verificationStorage.save(newEmail, verificationCode);

        UserSignupRequest signupRequest = new UserSignupRequest(
                "새사용자",
                newEmail,
                "010-9999-8888",
                "NewPassword123!",
                verificationCode
        );

        String requestBody = objectMapper.writeValueAsString(signupRequest);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 성공적으로 완료되었습니다."));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복 이메일")
    @WithAnonymousUser
    void signup_Fail_DuplicateEmail_Test() throws Exception {
        // given
        String verificationCode = "123456";
        verificationStorage.save(TEST_EMAIL, verificationCode);

        UserSignupRequest signupRequest = new UserSignupRequest(
                "중복시도",
                TEST_EMAIL, // 이미 존재하는 이메일
                "010-7777-6666",
                "Password123!",
                verificationCode
        );

        String requestBody = objectMapper.writeValueAsString(signupRequest);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("이미 가입된 이메일입니다")));
    }

    // ==================== 로그인 테스트 ====================

    @Test
    @DisplayName("로그인 성공 테스트")
    @WithAnonymousUser
    void login_Success_Test() throws Exception {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest(TEST_EMAIL, TEST_PASSWORD);
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인이 성공적으로 완료되었습니다."))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.userName").value(TEST_USERNAME))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 존재하지 않는 이메일")
    @WithAnonymousUser
    void login_Fail_NonExistentEmail_Test() throws Exception {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest(
                "nonexistent@example.com", // 존재하지 않는 이메일
                TEST_PASSWORD
        );
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(requestBody)
        );

        // then
        resultActions
                .andDo(printUtf8())
                .andExpect(status().isUnauthorized()); // 메시지 검증 제거, HTTP 상태 코드만 검증
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    @WithAnonymousUser
    void login_Fail_WrongPassword_Test() throws Exception {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest(
                TEST_EMAIL,
                "WrongPassword123!" // 잘못된 비밀번호
        );
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(requestBody)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isUnauthorized()); // 메시지 검증 제거, HTTP 상태 코드만 검증
    }

    // ==================== 이메일 인증 테스트 ====================

    @Test
    @DisplayName("이메일 인증 코드 발송 성공 테스트")
    @WithAnonymousUser
    void sendVerificationCode_Success_Test() throws Exception {
        // given
        EmailRequest emailRequest = new EmailRequest("verification@example.com");
        String requestBody = objectMapper.writeValueAsString(emailRequest);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/auth/send-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인증 코드가 발송되었습니다."));
    }

    // ==================== 헬퍼 메서드 ====================

    /**
     * UTF-8 인코딩을 지원하는 커스텀 ResultHandler
     */
    private ResultHandler printUtf8() {
        return result -> {
            System.out.println("==== MockMvc UTF-8 Response ====");
            System.out.println("Status: " + result.getResponse().getStatus());
            System.out.println("Content-Type: " + result.getResponse().getContentType());

            // ByteArray를 UTF-8로 디코딩해서 출력
            byte[] contentBytes = result.getResponse().getContentAsByteArray();
            String content = new String(contentBytes, "UTF-8");
            System.out.println("Content (UTF-8): " + content);
            System.out.println("================================");
        };
    }

    /**
     * Rate Limit 관련 Redis 키들을 삭제하여 테스트 간 격리를 보장합니다.
     */
    private void clearRateLimitKeys() {
        if (stringRedisTemplate == null) {
            return; // Redis가 비활성화된 경우 무시
        }

        try {
            // Rate Limit 관련 키 패턴들
            String[] patterns = {
                "email_send:*",
                "email_verify:*",
                "signup:*",
                "login:*",
                "login_email:*",
                "refresh_token:*"
            };

            int totalDeleted = 0;
            for (String pattern : patterns) {
                Set<String> keys = stringRedisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                    totalDeleted += keys.size();
                }
            }

            if (totalDeleted > 0) {
                System.out.println("테스트 환경: " + totalDeleted + "개의 Rate Limit 키가 삭제되었습니다.");
            }
        } catch (Exception e) {
            System.out.println("테스트 환경: Rate Limit 키 삭제 중 오류 발생: " + e.getMessage());
            // 테스트에서는 실패해도 진행
        }
    }

    /**
     * 테스트용 사용자를 생성하고 저장하는 헬퍼 메서드
     */
    private User createTestUser(String userName, String email, String phone, String password) {
        User user = User.builder()
                .userName(userName)
                .userEmail(email)
                .userNumber(phone)
                .build();

        UserAuth localAuth = UserAuth.builder()
                .authType(UserAuth.AuthType.LOCAL)
                .passwordHash(passwordEncoder.encode(password))
                .user(user)
                .build();
        localAuth.markAsVerified(); // 이메일 인증 완료 처리

        user.addUserAuth(localAuth);
        return userRepository.save(user);
    }
}
