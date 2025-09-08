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
 * Redis 폴백 기능 테스트
 * Redis가 비활성화된 상태에서 RDB로 자동 폴백되는지 확인
 */
@SpringBootTest
@AutoConfigureMockMvc  
@Transactional
@ActiveProfiles("redis-fallback-test") // Redis 비활성화 프로파일
class RedisFallbackTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VerificationStorage verificationStorage; // RDB 폴백 저장소

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Redis 비활성화 상태에서 RDB 폴백으로 회원가입 성공")
    void signup_success_with_rdb_fallback() throws Exception {
        // given: Redis 대신 RDB 폴백 저장소 사용
        String email = "fallback@example.com";
        String verificationCode = "654321";
        
        verificationStorage.save(email, verificationCode);

        UserSignupRequest request = UserSignupRequest.builder()
                .userName("폴백테스트")
                .email(email)
                .password("Password123!")
                .userNumber("010-9876-5432")
                .verificationCode(verificationCode)
                .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // RDB에 정상 저장 확인
        User savedUser = userRepository.findByUserEmail(email).orElseThrow();
        assertThat(savedUser.getUserName()).isEqualTo("폴백테스트");
        
        // 폴백 저장소에서 인증 코드 삭제 확인
        assertThat(verificationStorage.find(email)).isEmpty();
    }

    @Test
    @DisplayName("Redis 폴백 상태에서 잘못된 인증 코드로 회원가입 실패")
    void signup_fail_invalid_code_with_rdb_fallback() throws Exception {
        // given
        String email = "fallback@example.com";
        verificationStorage.save(email, "654321");

        UserSignupRequest request = UserSignupRequest.builder()
                .userName("폴백테스트")
                .email(email)
                .password("Password123!")
                .userNumber("010-9876-5432")
                .verificationCode("111111") // 잘못된 코드
                .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}