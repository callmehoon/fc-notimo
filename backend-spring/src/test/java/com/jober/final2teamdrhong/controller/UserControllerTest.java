package com.jober.final2teamdrhong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.changePassword.PasswordResetRequest;
import com.jober.final2teamdrhong.dto.user.DeleteUserRequest;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.util.test.WithMockJwtClaims;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

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
    private EntityManager entityManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            // 테스트 환경에 Redis가 없는 경우 무시
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 API")
    class ChangePasswordTests {

        @Test
        @WithMockJwtClaims(userId = 1, email = "test@example.com")
        @DisplayName("로컬 계정 비밀번호 변경 성공")
        void changePassword_Success() throws Exception {
            User testUser = User.create("테스트사용자", "test@example.com", "010-1234-5678");
            userRepository.saveAndFlush(testUser);
            UserAuth userAuth = UserAuth.createLocalAuth(testUser, passwordEncoder.encode("oldPassword123!"));
            testUser.addUserAuth(userAuth);
            userRepository.saveAndFlush(testUser);

            PasswordResetRequest request = new PasswordResetRequest("oldPassword123!", "newPassword456!");

            mockMvc.perform(put("/users/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            User updatedUser = userRepository.findById(testUser.getUserId()).orElseThrow();
            UserAuth updatedAuth = updatedUser.getUserAuths().stream().filter(auth -> auth.getAuthType() == UserAuth.AuthType.LOCAL).findFirst().orElseThrow();
            assertThat(passwordEncoder.matches("newPassword456!", updatedAuth.getPasswordHash())).isTrue();
        }

        @Test
        @WithMockJwtClaims(userId = 1, email = "test@example.com")
        @DisplayName("현재 비밀번호 불일치로 변경 실패")
        void changePassword_WrongCurrentPassword() throws Exception {
            User testUser = User.create("테스트사용자", "test@example.com", "010-1234-5678");
            userRepository.saveAndFlush(testUser);
            UserAuth userAuth = UserAuth.createLocalAuth(testUser, passwordEncoder.encode("oldPassword123!"));
            testUser.addUserAuth(userAuth);
            userRepository.saveAndFlush(testUser);

            PasswordResetRequest request = new PasswordResetRequest("wrongPassword123!", "newPassword456!");

            mockMvc.perform(put("/users/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 API")
    class DeleteAccountTests {

        @Test
        @WithMockJwtClaims(userId = 1, email = "test@example.com")
        @DisplayName("로컬 계정 회원 탈퇴 성공")
        void deleteAccount_LocalAuth_Success() throws Exception {
            User testUser = User.create("테스트사용자", "test@example.com", "010-1234-5678");
            userRepository.saveAndFlush(testUser);
            UserAuth userAuth = UserAuth.createLocalAuth(testUser, passwordEncoder.encode("password123!"));
            testUser.addUserAuth(userAuth);
            userRepository.saveAndFlush(testUser);

            DeleteUserRequest request = new DeleteUserRequest("password123!", "회원탈퇴");

            mockMvc.perform(delete("/users/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            assertThat(userRepository.findById(testUser.getUserId())).isEmpty();
        }

        @Test
        @WithMockJwtClaims(userId = 1, email = "test@example.com")
        @DisplayName("소셜 로그인 전용 사용자 회원 탈퇴 성공")
        void deleteAccount_SocialAuth_Success() throws Exception {
            User testUser = User.create("소셜사용자", "test@example.com", "010-1234-5678");
            userRepository.saveAndFlush(testUser);
            UserAuth socialAuth = UserAuth.createSocialAuth(testUser, UserAuth.AuthType.GOOGLE, "google123");
            testUser.addUserAuth(socialAuth);
            userRepository.saveAndFlush(testUser);

            DeleteUserRequest request = new DeleteUserRequest("social_user_dummy_password", "회원탈퇴");

            mockMvc.perform(delete("/users/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            assertThat(userRepository.findById(testUser.getUserId())).isEmpty();
        }

        @Test
        @WithMockJwtClaims(userId = 1, email = "test@example.com")
        @DisplayName("로컬 계정 - 비밀번호 불일치로 탈퇴 실패")
        void deleteAccount_WrongPassword() throws Exception {
            User testUser = User.create("테스트사용자", "test@example.com", "010-1234-5678");
            userRepository.saveAndFlush(testUser);
            UserAuth userAuth = UserAuth.createLocalAuth(testUser, passwordEncoder.encode("correctPassword123!"));
            testUser.addUserAuth(userAuth);
            userRepository.saveAndFlush(testUser);

            DeleteUserRequest request = new DeleteUserRequest("wrongPassword123!", "회원탈퇴");

            mockMvc.perform(delete("/users/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}