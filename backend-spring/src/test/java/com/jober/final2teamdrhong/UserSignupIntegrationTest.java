package com.jober.final2teamdrhong;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.UserSignupRequest;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 핵심 기능 통합 테스트
 * - 로컬 회원가입 
 * - Redis 기반 인증 코드 저장
 * - 원격 MySQL RDB 저장
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserSignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VerificationStorage verificationStorage;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공 - 유효한 인증 코드")
    void signup_success_with_valid_verification_code() throws Exception {
        // given
        String email = "test@example.com";
        String verificationCode = "123456";
        
        // 인증 코드를 저장소에 미리 저장
        verificationStorage.save(email, verificationCode);

        UserSignupRequest request = UserSignupRequest.builder()
                .userName("테스트사용자")
                .email(email)
                .password("Password123!")
                .userNumber("010-1234-5678")
                .verificationCode(verificationCode)
                .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 성공적으로 완료되었습니다."));

        // 데이터베이스 확인
        User savedUser = userRepository.findByUserEmail(email).orElseThrow();
        assertThat(savedUser.getUserName()).isEqualTo("테스트사용자");
        assertThat(savedUser.getUserRole()).isEqualTo(User.UserRole.USER);

        // 사용된 인증 코드가 삭제되었는지 확인
        assertThat(verificationStorage.find(email)).isEmpty();
    }

    @Test
    @DisplayName("회원가입 실패 - 잘못된 인증 코드")
    void signup_fail_with_invalid_verification_code() throws Exception {
        // given
        String email = "test@example.com";
        verificationStorage.save(email, "123456");

        UserSignupRequest request = UserSignupRequest.builder()
                .userName("테스트사용자")
                .email(email)
                .password("Password123!")
                .userNumber("010-1234-5678")
                .verificationCode("999999") // 잘못된 코드
                .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 인증 코드 발송 성공")
    void send_verification_code_success() throws Exception {
        // given
        String requestJson = "{\"email\":\"test@example.com\"}";

        // when & then
        mockMvc.perform(post("/auth/send-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증 코드가 이메일로 발송되었습니다."));
    }
}