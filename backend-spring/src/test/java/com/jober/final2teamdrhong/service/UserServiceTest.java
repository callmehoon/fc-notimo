package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.config.AuthProperties;
import com.jober.final2teamdrhong.dto.changePassword.PasswordResetRequest;
import com.jober.final2teamdrhong.dto.changePassword.ConfirmPasswordResetRequest;
import com.jober.final2teamdrhong.dto.user.DeleteUserRequest;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.exception.AuthenticationException;
import com.jober.final2teamdrhong.exception.BusinessException;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import com.jober.final2teamdrhong.util.TimingAttackProtection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private VerificationStorage verificationStorage;
    @Mock
    private TimingAttackProtection timingAttackProtection;
    @Mock
    private AuthProperties authProperties;
    @Mock
    private AuthProperties.Security authSecurityProperties;

    private User user;
    private UserAuth userAuth;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .userEmail("test@example.com")
                .userName("testuser")
                .userNumber("0101234-5678")
                .build();

        userAuth = UserAuth.builder()
                .authId(1)
                .authType(UserAuth.AuthType.LOCAL)
                .passwordHash("oldPasswordHash")
                .user(user)
                .build();

        user.addUserAuth(userAuth);
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class ChangePasswordTest {

        @Test
        @DisplayName("성공: 유효한 요청 시 비밀번호 변경 성공")
        void changePassword_Success() {
            // given
            long delay = 300L;
            PasswordResetRequest request = new PasswordResetRequest("oldPassword", "newPassword");

            given(userRepository.findById(1)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("oldPassword", "oldPasswordHash")).willReturn(true);
            given(passwordEncoder.matches("newPassword", "oldPasswordHash")).willReturn(false);
            given(passwordEncoder.encode("newPassword")).willReturn("newPasswordHash");
            given(authProperties.getSecurity()).willReturn(authSecurityProperties);
            given(authSecurityProperties.getTimingAttackDelayMs()).willReturn(delay);
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).ensureMinimumResponseTime(delay);
            willDoNothing().given(timingAttackProtection).clear();

            // when
            userService.changePassword(1, request, "127.0.0.1");

            // then
            assertThat(userAuth.getPasswordHash()).isEqualTo("newPasswordHash");
            then(tokenService).should().addAllUserTokensToBlacklist(1);
            then(rateLimitService).should().resetLoginRateLimit("test@example.com", "127.0.0.1");
        }

        @Test
        @DisplayName("실패: 현재 비밀번호 불일치")
        void changePassword_Fail_WrongCurrentPassword() {
            // given
            long delay = 300L;
            PasswordResetRequest request = new PasswordResetRequest("wrongOldPassword", "newPassword");

            given(userRepository.findById(1)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongOldPassword", "oldPasswordHash")).willReturn(false);
            given(authProperties.getSecurity()).willReturn(authSecurityProperties);
            given(authSecurityProperties.getTimingAttackDelayMs()).willReturn(delay);
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).ensureMinimumResponseTime(delay);
            willDoNothing().given(timingAttackProtection).clear();

            // when & then
            assertThatThrownBy(() -> userService.changePassword(1, request, "127.0.0.1"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("현재 비밀번호가 일치하지 않습니다.");
        }

        @Test
        @DisplayName("실패: 새 비밀번호가 현재 비밀번호와 동일")
        void changePassword_Fail_NewPasswordSameAsOld() {
            // given
            long delay = 300L;
            PasswordResetRequest request = new PasswordResetRequest("oldPassword", "oldPassword");

            given(userRepository.findById(1)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("oldPassword", "oldPasswordHash")).willReturn(true);
            given(passwordEncoder.matches("oldPassword", "oldPasswordHash")).willReturn(true);
            given(authProperties.getSecurity()).willReturn(authSecurityProperties);
            given(authSecurityProperties.getTimingAttackDelayMs()).willReturn(delay);
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).ensureMinimumResponseTime(delay);
            willDoNothing().given(timingAttackProtection).clear();

            // when & then
            assertThatThrownBy(() -> userService.changePassword(1, request, "127.0.0.1"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정 테스트")
    class ResetPasswordTest {

        @Test
        @DisplayName("성공: 유효한 인증 코드로 비밀번호 재설정 성공")
        void resetPassword_Success() {
            // given
            ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest("test@example.com", "validCode", "newPassword");

            willDoNothing().given(rateLimitService).checkEmailVerifyRateLimit("test@example.com");
            given(verificationStorage.validateAndDelete("test@example.com", "validCode")).willReturn(true);
            given(userRepository.findByUserEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.encode("newPassword")).willReturn("newPasswordHash");

            // when
            userService.resetPassword(request, "127.0.0.1");

            // then
            assertThat(userAuth.getPasswordHash()).isEqualTo("newPasswordHash");
            then(tokenService).should().addAllUserTokensToBlacklist(1);
            then(rateLimitService).should().resetLoginRateLimit("test@example.com", "127.0.0.1");
        }

        @Test
        @DisplayName("실패: 유효하지 않은 인증 코드")
        void resetPassword_Fail_InvalidVerificationCode() {
            // given
            ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest("test@example.com", "invalidCode", "newPassword");

            willDoNothing().given(rateLimitService).checkEmailVerifyRateLimit("test@example.com");
            given(verificationStorage.validateAndDelete("test@example.com", "invalidCode")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.resetPassword(request, "127.0.0.1"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("인증 코드가 일치하지 않거나 만료되었습니다.");
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 테스트")
    class DeleteAccountTest {

        @Test
        @DisplayName("로컬 계정 회원 탈퇴 성공 테스트")
        void shouldDeleteLocalAccountSuccessfully() {
            // given
            long delay = 300L;
            Integer userId = 1;
            String password = "Password123!";
            String clientIp = "192.168.1.1";
            DeleteUserRequest request = new DeleteUserRequest(password, "회원탈퇴");

            User localUser = User.builder().userId(userId).userEmail("local@example.com").build();
            UserAuth localAuth = UserAuth.createLocalAuth(localUser, "encoded_password_hash");
            localUser.addUserAuth(localAuth);

            given(userRepository.findById(userId)).willReturn(Optional.of(localUser));
            given(authProperties.getSecurity()).willReturn(authSecurityProperties);
            given(authSecurityProperties.getAnonymizedEmailFormat()).willReturn("deleted_user_%d_%d@deleted.com");
            given(authSecurityProperties.getTimingAttackDelayMs()).willReturn(delay);
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).ensureMinimumResponseTime(delay);
            willDoNothing().given(timingAttackProtection).clear();
            given(passwordEncoder.matches(password, "encoded_password_hash")).willReturn(true);

            // when
            userService.deleteAccount(userId, request, clientIp);

            // then
            then(tokenService).should().addAllUserTokensToBlacklist(userId);
            then(rateLimitService).should().resetLoginRateLimit("local@example.com", clientIp);
            assertThat(localUser.getIsDeleted()).isTrue();
            assertThat(localUser.getUserEmail()).startsWith("deleted_user_" + userId + "_");
        }

        @Test
        @DisplayName("소셜 계정 회원 탈퇴 성공 테스트 (비밀번호 검증 없음)")
        void shouldDeleteSocialAccountSuccessfully() {
            // given
            Integer userId = 1;
            String clientIp = "192.168.1.1";
            DeleteUserRequest request = new DeleteUserRequest("any_password", "회원탈퇴");

            User socialUser = User.builder().userId(userId).userEmail("social@example.com").build();
            UserAuth googleAuth = UserAuth.createSocialAuth(socialUser, UserAuth.AuthType.GOOGLE, "google123");
            socialUser.addUserAuth(googleAuth);

            given(userRepository.findById(userId)).willReturn(Optional.of(socialUser));
            given(authProperties.getSecurity()).willReturn(authSecurityProperties);
            given(authSecurityProperties.getAnonymizedEmailFormat()).willReturn("deleted_user_%d_%d@deleted.com");

            // when
            userService.deleteAccount(userId, request, clientIp);

            // then
            then(passwordEncoder).should(never()).matches(anyString(), anyString());
            then(tokenService).should().addAllUserTokensToBlacklist(userId);
            then(rateLimitService).should().resetLoginRateLimit("social@example.com", clientIp);
            assertThat(socialUser.getIsDeleted()).isTrue();
        }

        @Test
        @DisplayName("비밀번호 불일치 시 회원 탈퇴 실패 테스트")
        void shouldFailWhenPasswordMismatch() {
            // given
            long delay = 300L;
            Integer userId = 1;
            String wrongPassword = "WrongPassword!";
            DeleteUserRequest request = new DeleteUserRequest(wrongPassword, "회원탈퇴");

            User localUser = User.builder().userId(userId).userEmail("local@example.com").build();
            UserAuth localAuth = UserAuth.createLocalAuth(localUser, "encoded_password_hash");
            localUser.addUserAuth(localAuth);

            given(userRepository.findById(userId)).willReturn(Optional.of(localUser));
            given(authProperties.getSecurity()).willReturn(authSecurityProperties);
            given(authSecurityProperties.getTimingAttackDelayMs()).willReturn(delay);
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).ensureMinimumResponseTime(delay);
            willDoNothing().given(timingAttackProtection).clear();
            given(passwordEncoder.matches(wrongPassword, "encoded_password_hash")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.deleteAccount(userId, request, "127.0.0.1"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");

            assertThat(localUser.getIsDeleted()).isFalse();
            then(tokenService).should(never()).addAllUserTokensToBlacklist(any());
        }

        @Test
        @DisplayName("이미 탈퇴한 회원의 탈퇴 시도 시 실패 테스트")
        void shouldFailWhenUserAlreadyDeleted() {
            // given
            Integer userId = 1;
            DeleteUserRequest request = new DeleteUserRequest("any_password", "회원탈퇴");

            User deletedUser = User.builder().userId(userId).build();
            deletedUser.deleteAccount("deleted@email.com");

            given(userRepository.findById(userId)).willReturn(Optional.of(deletedUser));

            // when & then
            assertThatThrownBy(() -> userService.deleteAccount(userId, request, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 탈퇴 처리된 계정입니다.");
        }
    }
}
