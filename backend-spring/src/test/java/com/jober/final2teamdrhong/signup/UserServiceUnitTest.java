package com.jober.final2teamdrhong.signup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.UserSignupRequestDto;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class UserServiceSignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VerificationStorage verificationStorage; // ✅ 인증 코드 저장소 주입

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("성공: 유효한 인증 코드와 함께 회원가입 요청 시 DB에 사용자가 저장된다")
    void signup_success() throws Exception {
        // given: 이런 데이터가 주어졌을 때
        String userEmail = "test@test.com";
        String validCode = "123456";

        // 1. (가장 중요) 테스트를 위해 미리 인증 코드를 저장소에 저장해 둔다.
        verificationStorage.save(userEmail, validCode);

        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .userName("홍길동")
                .email(userEmail)
                .password("Password123!")
                .userNumber("01012345678")
                .verificationCode(validCode) // 올바른 인증 코드 포함
                .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when: 이 API를 호출하면
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk()); // then: 200 OK 응답이 와야 한다.

        // then: 그리고 DB를 확인해보면
        User savedUser = userRepository.findByUserEmail(userEmail).orElseThrow();
        assertThat(savedUser.getUserName()).isEqualTo("홍길동");
        assertThat(savedUser.getUserRole()).isEqualTo(User.UserRole.USER);
        assertThat(savedUser.getUserAuths()).hasSize(1); // 인증 정보도 함께 저장되었는지 확인

        // then: 사용된 인증 코드가 저장소에서 삭제되었는지 확인
        assertThat(verificationStorage.find(userEmail)).isEmpty();
    }

    @Test
    @DisplayName("실패: 인증 코드가 일치하지 않으면 회원가입에 실패한다 (400 Bad Request)")
    void signup_fail_invalidCode() throws Exception {
        // given:
        String userEmail = "test@test.com";
        verificationStorage.save(userEmail, "123456"); // 저장된 코드는 "123456"

        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .userName("홍길동")
                .email(userEmail)
                .password("Password123!")
                .verificationCode("999999") // ❌ 잘못된 인증 코드
                .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then: API 호출 시 400 Bad Request 에러를 기대
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 중복된 이메일로 회원가입 요청 시 409 Conflict 에러가 발생한다")
    void signup_fail_duplicateEmail() throws Exception {
        // given: 이미 완전한 정보의 사용자가 DB에 존재하고,
        String userEmail = "test@test.com";

        // ✅ 1. User와 UserAuth를 모두 갖춘 완전한 기존 사용자를 생성합니다.
        User existingUser = User.builder()
                .userEmail(userEmail)
                .userName("기존유저")
                .build();
        UserAuth localAuth = UserAuth.builder()
                .authType(UserAuth.AuthType.LOCAL)
                .passwordHash("hashed_password_for_test")
                .build();
        existingUser.addUserAuth(localAuth);
        userRepository.save(existingUser);


        // given: 인증 코드는 유효하더라도,
        String validCode = "123456";
        verificationStorage.save(userEmail, validCode);

        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .userName("새로운유저")
                .email(userEmail) // 중복된 이메일
                .password("Password123!")
                .verificationCode(validCode)
                .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then: API 호출 시 400 Bad Request 에러를 기대 (중복 이메일 예외 처리)
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}

