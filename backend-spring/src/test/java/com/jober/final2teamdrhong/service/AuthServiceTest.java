package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.config.AuthProperties;
import com.jober.final2teamdrhong.config.JwtConfig;
import com.jober.final2teamdrhong.dto.auth.SocialSignupRequest;
import com.jober.final2teamdrhong.dto.auth.SocialSignupResponse;
import com.jober.final2teamdrhong.dto.userLogin.TokenRefreshResponse;
import com.jober.final2teamdrhong.dto.userLogin.UserLoginRequest;
import com.jober.final2teamdrhong.dto.userLogin.UserLoginResponse;
import com.jober.final2teamdrhong.dto.userSignup.UserSignupRequest;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

/**
 * AuthService 통합 테스트
 *
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // === 모든 의존성 Mock 객체들 ===
    @Mock
    private VerificationStorage verificationStorage;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private AuthProperties authProperties;

    @Mock
    private AuthProperties.Security securityProperties;

    @Mock
    private AuthProperties.Messages messagesProperties;

    @Mock
    private TimingAttackProtection timingAttackProtection;


    @Mock
    private UserValidationService userValidationService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // 테스트별로 필요한 Mock 설정은 각 테스트에서 개별적으로 설정합니다.
        // 공통으로 사용되지 않는 Mock 설정은 setUp에서 제거했습니다.
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTest {

        @Test
        @DisplayName("Rate Limit과 함께 회원가입 성공 테스트")
        void shouldSignupSuccessfullyWithRateLimit() {
            // given
            // 1. 회원가입 요청 정보를 준비합니다.
            UserSignupRequest request = new UserSignupRequest(
                    "테스트사용자",
                    "test@example.com",
                    "010-1234-5678",
                    "Password123!",
                    "123456"
            );
            String clientIp = "192.168.1.1";

            // 2. Rate Limit 검사가 통과하도록 설정합니다.
            willDoNothing().given(rateLimitService).checkSignupRateLimit(clientIp, request.email());

            // 3. 비즈니스 규칙 검증이 통과하도록 설정합니다.
            willDoNothing().given(userValidationService).validateLocalSignupBusinessRules(request);

            // 4. 이메일 인증 코드 검증이 성공하도록 설정합니다.
            willDoNothing().given(rateLimitService).checkEmailVerifyRateLimit(request.email());
            given(verificationStorage.validateAndDelete(request.email(), request.verificationCode())).willReturn(true);

            // 5. 비밀번호 암호화 설정을 합니다.
            given(passwordEncoder.encode(request.password())).willReturn("$2a$10$encoded.password.hash");

            // 6. 사용자 저장이 성공하도록 설정합니다.
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                // ID 설정을 시뮬레이션합니다.
                try {
                    var field = user.getClass().getDeclaredField("userId");
                    field.setAccessible(true);
                    field.set(user, 1);
                } catch (Exception e) {
                    // 테스트에서는 무시
                }
                return user;
            });

            // when
            // 1. Rate Limit과 함께 회원가입을 수행합니다.
            authService.signupWithRateLimit(request, clientIp);

            // then
            // 1. Rate Limit 검사가 수행되었는지 확인합니다.
            then(rateLimitService).should(times(1)).checkSignupRateLimit(clientIp, request.email());

            // 2. 비즈니스 규칙 검증이 수행되었는지 확인합니다.
            then(userValidationService).should(times(1)).validateLocalSignupBusinessRules(request);

            // 3. 이메일 인증 코드 검증이 수행되었는지 확인합니다.
            then(verificationStorage).should(times(1)).validateAndDelete(request.email(), request.verificationCode());

            // 4. 비밀번호가 암호화되었는지 확인합니다.
            then(passwordEncoder).should(times(1)).encode(request.password());

            // 5. 사용자가 저장되었는지 확인합니다.
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should(times(1)).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getUserName()).isEqualTo("테스트사용자");
            assertThat(savedUser.getUserEmail()).isEqualTo("test@example.com");
            assertThat(savedUser.getUserNumber()).isEqualTo("010-1234-5678");

            // 6. UserAuth가 올바르게 설정되었는지 확인합니다.
            List<UserAuth> userAuths = savedUser.getUserAuths();
            assertThat(userAuths).hasSize(1);
            UserAuth userAuth = userAuths.get(0);
            assertThat(userAuth.getAuthType()).isEqualTo(UserAuth.AuthType.LOCAL);
            assertThat(userAuth.getPasswordHash()).isEqualTo("$2a$10$encoded.password.hash");
        }

        @Test
        @DisplayName("Rate Limit 초과 시 회원가입 실패 테스트")
        void shouldFailWhenRateLimitExceeded() {
            // given
            // 1. 회원가입 요청 정보를 준비합니다.
            UserSignupRequest request = new UserSignupRequest(
                    "테스트사용자",
                    "test@example.com",
                    "010-1234-5678",
                    "Password123!",
                    "123456"
            );
            String clientIp = "192.168.1.1";

            // 2. Rate Limit 초과 예외가 발생하도록 설정합니다.
            RuntimeException rateLimitException = new RuntimeException("Too many signup attempts");
            willThrow(rateLimitException).given(rateLimitService).checkSignupRateLimit(clientIp, request.email());

            // when & then
            // 1. Rate Limit 초과로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.signupWithRateLimit(request, clientIp))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Too many signup attempts");

            // 2. 후속 검증들이 수행되지 않았는지 확인합니다.
            then(userValidationService).should(never()).validateLocalSignupBusinessRules(any());
            then(verificationStorage).should(never()).validateAndDelete(anyString(), anyString());
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("비즈니스 규칙 검증 실패 시 회원가입 실패 테스트")
        void shouldFailWhenBusinessRuleValidationFails() {
            // given
            // 1. 회원가입 요청 정보를 준비합니다.
            UserSignupRequest request = new UserSignupRequest(
                    "테스트사용자",
                    "existing@example.com", // 이미 존재하는 이메일
                    "010-1234-5678",
                    "Password123!",
                    "123456"
            );
            String clientIp = "192.168.1.1";

            // 2. Rate Limit 검사가 통과하도록 설정합니다.
            willDoNothing().given(rateLimitService).checkSignupRateLimit(clientIp, request.email());

            // 3. 비즈니스 규칙 검증이 실패하도록 설정합니다.
            BusinessException businessException = new BusinessException("이미 존재하는 이메일입니다.");
            willThrow(businessException).given(userValidationService).validateLocalSignupBusinessRules(request);

            // when & then
            // 1. 비즈니스 규칙 검증 실패로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.signupWithRateLimit(request, clientIp))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("이미 존재하는 이메일입니다.");

            // 2. Rate Limit 검사는 수행되었는지 확인합니다.
            then(rateLimitService).should(times(1)).checkSignupRateLimit(clientIp, request.email());

            // 3. 후속 검증들이 수행되지 않았는지 확인합니다.
            then(verificationStorage).should(never()).validateAndDelete(anyString(), anyString());
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("인증 코드 검증 실패 시 회원가입 실패 테스트")
        void shouldFailWhenVerificationCodeIsInvalid() {
            // given
            // 1. 회원가입 요청 정보를 준비합니다.
            UserSignupRequest request = new UserSignupRequest(
                    "테스트사용자",
                    "test@example.com",
                    "010-1234-5678",
                    "Password123!",
                    "wrong123" // 잘못된 인증 코드
            );
            String clientIp = "192.168.1.1";

            // 2. Rate Limit 검사가 통과하도록 설정합니다.
            willDoNothing().given(rateLimitService).checkSignupRateLimit(clientIp, request.email());

            // 3. 비즈니스 규칙 검증이 통과하도록 설정합니다.
            willDoNothing().given(userValidationService).validateLocalSignupBusinessRules(request);

            // 4. 이메일 인증 코드 Rate Limit이 통과하도록 설정합니다.
            willDoNothing().given(rateLimitService).checkEmailVerifyRateLimit(request.email());

            // 5. 인증 코드 검증이 실패하도록 설정합니다.
            given(verificationStorage.validateAndDelete(request.email(), request.verificationCode())).willReturn(false);

            // when & then
            // 1. 인증 코드 검증 실패로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.signupWithRateLimit(request, clientIp))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("인증 코드가 일치하지 않거나 만료되었습니다.");

            // 2. 인증 코드 검증은 수행되었는지 확인합니다.
            then(verificationStorage).should(times(1)).validateAndDelete(request.email(), request.verificationCode());

            // 3. 사용자 저장은 수행되지 않았는지 확인합니다.
            then(userRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("유효한 인증 정보로 로그인 성공 테스트")
        void shouldLoginSuccessfullyWithValidCredentials() {
            // given
            // 1. 로그인 요청 정보를 준비합니다.
            UserLoginRequest request = new UserLoginRequest("test@example.com", "Password123!");
            String clientIp = "192.168.1.1";

            // 2. 기존 사용자와 로컬 인증 정보를 생성합니다.
            User existingUser = User.create("기존사용자", "test@example.com", "010-1234-5678");
            UserAuth localAuth = UserAuth.createLocalAuth(existingUser, "$2a$10$encoded.password.hash");
            existingUser.addUserAuth(localAuth);

            // 3. 타이밍 공격 방지를 위한 설정을 합니다.
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).clear();

            // 4. AuthProperties 설정을 합니다 (성공 테스트에서는 더미 해시만 필요).
            given(authProperties.getSecurity()).willReturn(securityProperties);
            given(securityProperties.getDummyHash()).willReturn("$2a$10$dummy.hash.for.timing.attack");

            // 5. 사용자 조회가 성공하도록 설정합니다.
            given(userRepository.findByUserEmailWithAuth(request.email())).willReturn(Optional.of(existingUser));

            // 6. 비밀번호 매칭이 성공하도록 설정합니다.
            given(passwordEncoder.matches(request.password(), "$2a$10$encoded.password.hash")).willReturn(true);

            // 7. User ID를 설정합니다 (JWT 토큰 생성에 필요).
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                try {
                    var field = user.getClass().getDeclaredField("userId");
                    field.setAccessible(true);
                    field.set(user, 1);
                } catch (Exception e) {
                    // 테스트에서는 무시
                }
                return user;
            });

            // 8. 토큰 생성 설정을 합니다 (동적 ID 때문에 any()를 사용).
            given(jwtConfig.generateAccessToken(eq(existingUser.getUserEmail()), any())).willReturn("access_token_123");
            given(refreshTokenService.createRefreshToken(existingUser, clientIp)).willReturn("refresh_token_123");

            // when
            // 1. 로그인을 수행합니다.
            UserLoginResponse response = authService.loginWithRefreshToken(request, clientIp);

            // then
            // 1. 응답이 올바른지 확인합니다.
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("access_token_123");
            assertThat(response.refreshToken()).isEqualTo("refresh_token_123");

            // 2. 타이밍 공격 방지가 수행되었는지 확인합니다.
            then(timingAttackProtection).should(times(1)).startTiming();
            then(timingAttackProtection).should(times(1)).clear();

            // 3. 사용자 조회가 수행되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmailWithAuth(request.email());

            // 4. 비밀번호 검증이 수행되었는지 확인합니다.
            then(passwordEncoder).should(times(1)).matches(request.password(), "$2a$10$encoded.password.hash");

            // 5. 토큰 생성이 수행되었는지 확인합니다.
            then(jwtConfig).should(times(1)).generateAccessToken(existingUser.getUserEmail(), existingUser.getUserId());
            then(refreshTokenService).should(times(1)).createRefreshToken(existingUser, clientIp);

            // 6. 사용자 업데이트 저장이 수행되었는지 확인합니다.
            then(userRepository).should(times(1)).save(existingUser);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패 테스트")
        void shouldFailWhenUserNotFound() {
            // given
            // 1. 존재하지 않는 이메일로 로그인 요청을 준비합니다.
            UserLoginRequest request = new UserLoginRequest("notfound@example.com", "Password123!");
            String clientIp = "192.168.1.1";

            // 2. 타이밍 공격 방지를 위한 설정을 합니다.
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).clear();

            // 3. AuthProperties 설정을 합니다.
            given(authProperties.getSecurity()).willReturn(securityProperties);
            given(securityProperties.getDummyHash()).willReturn("$2a$10$dummy.hash.for.timing.attack");
            given(securityProperties.getMinResponseTimeMs()).willReturn(500);
            given(authProperties.getMessages()).willReturn(messagesProperties);
            given(messagesProperties.getInvalidCredentials()).willReturn("이메일 또는 비밀번호가 올바르지 않습니다.");

            // 4. 사용자 조회가 실패하도록 설정합니다.
            given(userRepository.findByUserEmailWithAuth(request.email())).willReturn(Optional.empty());

            // 5. 더미 해시와 비밀번호 매칭이 실패하도록 설정합니다.
            given(passwordEncoder.matches(request.password(), "$2a$10$dummy.hash.for.timing.attack")).willReturn(false);

            // 6. 타이밍 공격 방지를 위한 응답 시간 설정을 합니다.
            willDoNothing().given(timingAttackProtection).ensureMinimumResponseTime(500);

            // when & then
            // 1. 사용자가 존재하지 않아 로그인 실패 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.loginWithRefreshToken(request, clientIp))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

            // 2. 사용자 조회가 수행되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmailWithAuth(request.email());

            // 3. 더미 해시와 비밀번호 매칭이 수행되었는지 확인합니다.
            then(passwordEncoder).should(times(1)).matches(request.password(), "$2a$10$dummy.hash.for.timing.attack");

            // 4. 타이밍 공격 방지가 수행되었는지 확인합니다.
            then(timingAttackProtection).should(times(2)).ensureMinimumResponseTime(500);

            // 5. 토큰 생성은 수행되지 않았는지 확인합니다.
            then(jwtConfig).should(never()).generateAccessToken(anyString(), any());
            then(refreshTokenService).should(never()).createRefreshToken(any(), anyString());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패 테스트")
        void shouldFailWhenPasswordIsIncorrect() {
            // given
            // 1. 잘못된 비밀번호로 로그인 요청을 준비합니다.
            UserLoginRequest request = new UserLoginRequest("test@example.com", "WrongPassword!");
            String clientIp = "192.168.1.1";

            // 2. 기존 사용자와 로컬 인증 정보를 생성합니다.
            User existingUser = User.create("기존사용자", "test@example.com", "010-1234-5678");
            UserAuth localAuth = UserAuth.createLocalAuth(existingUser, "$2a$10$encoded.password.hash");
            existingUser.addUserAuth(localAuth);

            // 3. 타이밍 공격 방지를 위한 설정을 합니다.
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).clear();

            // 4. AuthProperties 설정을 합니다.
            given(authProperties.getSecurity()).willReturn(securityProperties);
            given(securityProperties.getDummyHash()).willReturn("$2a$10$dummy.hash.for.timing.attack");
            given(securityProperties.getMinResponseTimeMs()).willReturn(500);
            given(authProperties.getMessages()).willReturn(messagesProperties);
            given(messagesProperties.getInvalidCredentials()).willReturn("이메일 또는 비밀번호가 올바르지 않습니다.");

            // 5. 사용자 조회가 성공하도록 설정합니다.
            given(userRepository.findByUserEmailWithAuth(request.email())).willReturn(Optional.of(existingUser));

            // 6. 비밀번호 매칭이 실패하도록 설정합니다.
            given(passwordEncoder.matches(request.password(), "$2a$10$encoded.password.hash")).willReturn(false);

            // 7. 타이밍 공격 방지를 위한 응답 시간 설정을 합니다.
            willDoNothing().given(timingAttackProtection).ensureMinimumResponseTime(500);

            // when & then
            // 1. 잘못된 비밀번호로 인한 로그인 실패 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.loginWithRefreshToken(request, clientIp))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

            // 2. 사용자 조회가 수행되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmailWithAuth(request.email());

            // 3. 비밀번호 검증이 수행되었는지 확인합니다.
            then(passwordEncoder).should(times(1)).matches(request.password(), "$2a$10$encoded.password.hash");

            // 4. 타이밍 공격 방지가 수행되었는지 확인합니다.
            then(timingAttackProtection).should(times(2)).ensureMinimumResponseTime(500);

            // 5. 토큰 생성은 수행되지 않았는지 확인합니다.
            then(jwtConfig).should(never()).generateAccessToken(anyString(), any());
            then(refreshTokenService).should(never()).createRefreshToken(any(), anyString());
        }

        @Test
        @DisplayName("로컬 인증 정보가 없는 사용자로 로그인 실패 테스트")
        void shouldFailWhenUserHasNoLocalAuth() {
            // given
            // 1. 로그인 요청 정보를 준비합니다.
            UserLoginRequest request = new UserLoginRequest("social@example.com", "Password123!");
            String clientIp = "192.168.1.1";

            // 2. 소셜 로그인만 있는 사용자를 생성합니다.
            User socialOnlyUser = User.create("소셜사용자", "social@example.com", "010-1234-5678");
            UserAuth socialAuth = UserAuth.createSocialAuth(socialOnlyUser, UserAuth.AuthType.GOOGLE, "google123");
            socialOnlyUser.addUserAuth(socialAuth);

            // 3. 타이밍 공격 방지를 위한 설정을 합니다.
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).clear();

            // 4. AuthProperties 설정을 합니다.
            given(authProperties.getSecurity()).willReturn(securityProperties);
            given(securityProperties.getDummyHash()).willReturn("$2a$10$dummy.hash.for.timing.attack");
            given(securityProperties.getMinResponseTimeMs()).willReturn(500);
            given(authProperties.getMessages()).willReturn(messagesProperties);
            given(messagesProperties.getInvalidCredentials()).willReturn("이메일 또는 비밀번호가 올바르지 않습니다.");

            // 5. 사용자 조회가 성공하지만 로컬 인증이 없도록 설정합니다.
            given(userRepository.findByUserEmailWithAuth(request.email())).willReturn(Optional.of(socialOnlyUser));

            // 6. 더미 해시와 비밀번호 매칭이 실패하도록 설정합니다.
            given(passwordEncoder.matches(request.password(), "$2a$10$dummy.hash.for.timing.attack")).willReturn(false);

            // 7. 타이밍 공격 방지를 위한 응답 시간 설정을 합니다.
            willDoNothing().given(timingAttackProtection).ensureMinimumResponseTime(500);

            // when & then
            // 1. 로컬 인증 정보가 없어 로그인 실패 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.loginWithRefreshToken(request, clientIp))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

            // 2. 사용자 조회가 수행되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmailWithAuth(request.email());

            // 3. 더미 해시와 비밀번호 매칭이 수행되었는지 확인합니다.
            then(passwordEncoder).should(times(1)).matches(request.password(), "$2a$10$dummy.hash.for.timing.attack");

            // 4. 타이밍 공격 방지가 수행되었는지 확인합니다.
            then(timingAttackProtection).should(times(2)).ensureMinimumResponseTime(500);

            // 5. 토큰 생성은 수행되지 않았는지 확인합니다.
            then(jwtConfig).should(never()).generateAccessToken(anyString(), any());
            then(refreshTokenService).should(never()).createRefreshToken(any(), anyString());
        }

        @Test
        @DisplayName("예상치 못한 예외 발생 시 로그인 실패 테스트")
        void shouldHandleUnexpectedExceptionDuringLogin() {
            // given
            // 1. 로그인 요청 정보를 준비합니다.
            UserLoginRequest request = new UserLoginRequest("test@example.com", "Password123!");
            String clientIp = "192.168.1.1";

            // 2. 타이밍 공격 방지를 위한 설정을 합니다.
            willDoNothing().given(timingAttackProtection).startTiming();
            willDoNothing().given(timingAttackProtection).clear();

            // 3. AuthProperties 설정을 합니다.
            given(authProperties.getSecurity()).willReturn(securityProperties);
            given(securityProperties.getMinResponseTimeMs()).willReturn(500);
            given(authProperties.getMessages()).willReturn(messagesProperties);
            given(messagesProperties.getInvalidCredentials()).willReturn("이메일 또는 비밀번호가 올바르지 않습니다.");

            // 4. 사용자 조회에서 예상치 못한 예외가 발생하도록 설정합니다.
            RuntimeException unexpectedException = new RuntimeException("Database connection error");
            given(userRepository.findByUserEmailWithAuth(request.email())).willThrow(unexpectedException);

            // 5. 타이밍 공격 방지를 위한 응답 시간 설정을 합니다.
            willDoNothing().given(timingAttackProtection).ensureMinimumResponseTime(500);

            // when & then
            // 1. 예상치 못한 예외로 인한 로그인 실패 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.loginWithRefreshToken(request, clientIp))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

            // 2. 타이밍 공격 방지가 수행되었는지 확인합니다 (handleUnexpectedError는 1번만 호출).
            then(timingAttackProtection).should(times(1)).startTiming();
            then(timingAttackProtection).should(times(1)).clear();
            then(timingAttackProtection).should(times(1)).ensureMinimumResponseTime(500);

            // 3. 사용자 조회가 시도되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmailWithAuth(request.email());

            // 4. 토큰 생성은 수행되지 않았는지 확인합니다.
            then(jwtConfig).should(never()).generateAccessToken(anyString(), any());
            then(refreshTokenService).should(never()).createRefreshToken(any(), anyString());
        }
    }

    @Nested
    @DisplayName("소셜 회원가입 테스트")
    class SocialSignupTest {

        @Test
        @DisplayName("유효한 소셜 정보로 회원가입 성공 테스트")
        void shouldCompleteSocialSignupSuccessfully() {
            // given
            // 1. 새로운 아키텍처에 맞는 소셜 회원가입 요청 정보를 준비합니다.
            SocialSignupRequest request = new SocialSignupRequest(
                    "google",                // provider
                    "google_user_123",       // socialId
                    "social@example.com",    // email
                    "소셜사용자",              // name
                    "010-1234-5678",         // phoneNumber
                    true,                    // agreedToTerms
                    true,                    // agreedToPrivacyPolicy
                    false                    // agreedToMarketing
            );

            // 2. 비즈니스 규칙 검증이 통과하도록 설정합니다.
            willDoNothing().given(userValidationService).validateSocialSignupBusinessRules(
                    request.email(),
                    request.name(),
                    request
            );

            // 3. 사용자 저장이 성공하도록 설정합니다.
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                // ID 설정을 시뮬레이션합니다.
                try {
                    var field = user.getClass().getDeclaredField("userId");
                    field.setAccessible(true);
                    field.set(user, 1);
                } catch (Exception e) {
                    // 테스트에서는 무시
                }
                return user;
            });

            // 4. JWT 토큰 생성 설정을 합니다.
            given(jwtConfig.generateAccessToken(eq(request.email()), any())).willReturn("social_access_token");
            given(refreshTokenService.createRefreshToken(any(User.class), eq("social-signup"))).willReturn("social_refresh_token");

            // when
            // 1. 소셜 회원가입을 완료합니다.
            SocialSignupResponse response = authService.completeSocialSignup(request);

            // then
            // 1. 응답이 성공적으로 생성되었는지 확인합니다.
            assertThat(response).isNotNull();
            assertThat(response.success()).isTrue();
            assertThat(response.accessToken()).isEqualTo("social_access_token");
            assertThat(response.refreshToken()).isEqualTo("social_refresh_token");
            assertThat(response.email()).isEqualTo("social@example.com");
            assertThat(response.name()).isEqualTo("소셜사용자");
            assertThat(response.provider()).isEqualTo("google");

            // 2. 비즈니스 규칙 검증이 수행되었는지 확인합니다.
            then(userValidationService).should(times(1)).validateSocialSignupBusinessRules(
                    request.email(),
                    request.name(),
                    request
            );

            // 3. 사용자 저장이 수행되었는지 확인합니다.
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should(times(1)).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getUserName()).isEqualTo("소셜사용자");
            assertThat(savedUser.getUserEmail()).isEqualTo("social@example.com");
            assertThat(savedUser.getUserNumber()).isEqualTo("01012345678"); // 정규화된 번호 (하이픈 제거됨)
            assertThat(savedUser.getUserRole()).isEqualTo(User.UserRole.USER); // 명시적 USER 권한

            // 4. UserAuth가 올바르게 설정되었는지 확인합니다.
            assertThat(savedUser.getUserAuths()).hasSize(1);
            UserAuth savedAuth = savedUser.getUserAuths().get(0);
            assertThat(savedAuth.getAuthType()).isEqualTo(UserAuth.AuthType.GOOGLE);
            assertThat(savedAuth.getSocialId()).isEqualTo("google_user_123");
            assertThat(savedAuth.getIsVerified()).isTrue(); // 소셜 인증은 이미 완료된 상태

            // 5. JWT 토큰 생성이 수행되었는지 확인합니다.
            then(jwtConfig).should(times(1)).generateAccessToken(request.email(), savedUser.getUserId());
            then(refreshTokenService).should(times(1)).createRefreshToken(savedUser, "social-signup");
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 소셜 회원가입 실패 테스트")
        void shouldFailWhenEmailAlreadyExists() {
            // given
            // 1. 소셜 회원가입 요청 정보를 준비합니다.
            SocialSignupRequest request = new SocialSignupRequest(
                    "google",
                    "google_user_123",
                    "existing@example.com", // 이미 존재하는 이메일
                    "중복사용자",
                    "010-1234-5678",
                    true,
                    true,
                    false
            );

            // 2. 비즈니스 규칙 검증이 실패하도록 설정합니다.
            BusinessException businessException = new BusinessException("이미 존재하는 이메일입니다.");
            willThrow(businessException).given(userValidationService).validateSocialSignupBusinessRules(
                    request.email(),
                    request.name(),
                    request
            );

            // when & then
            // 1. 비즈니스 규칙 검증 실패로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.completeSocialSignup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("이미 존재하는 이메일입니다.");

            // 2. 비즈니스 규칙 검증은 수행되지만 후속 처리는 수행되지 않았는지 확인합니다.
            then(userValidationService).should(times(1)).validateSocialSignupBusinessRules(
                    request.email(),
                    request.name(),
                    request
            );
            then(userRepository).should(never()).save(any());
            then(jwtConfig).should(never()).generateAccessToken(anyString(), any());
        }

        @Test
        @DisplayName("필수 약관 미동의 시 소셜 회원가입 실패 테스트")
        void shouldFailWhenRequiredAgreementsNotAccepted() {
            // given
            // 1. 필수 약관에 동의하지 않은 소셜 회원가입 요청을 준비합니다.
            SocialSignupRequest request = new SocialSignupRequest(
                    "google",
                    "google_user_123",
                    "social@example.com",
                    "소셜사용자",
                    "010-1234-5678",
                    false,  // 약관 미동의
                    true,
                    false
            );

            // 2. 비즈니스 규칙 검증이 실패하도록 설정합니다.
            BusinessException businessException = new BusinessException("필수 약관에 동의해야 합니다.");
            willThrow(businessException).given(userValidationService).validateSocialSignupBusinessRules(
                    request.email(),
                    request.name(),
                    request
            );

            // when & then
            // 1. 필수 약관 미동의로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.completeSocialSignup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("필수 약관에 동의해야 합니다.");

            // 2. 후속 처리가 수행되지 않았는지 확인합니다.
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("잘못된 핸드폰 번호 형식으로 소셜 회원가입 실패 테스트")
        void shouldFailWhenPhoneNumberIsInvalid() {
            // given
            // 1. 잘못된 핸드폰 번호 형식의 소셜 회원가입 요청을 준비합니다.
            SocialSignupRequest request = new SocialSignupRequest(
                    "google",
                    "google_user_123",
                    "social@example.com",
                    "소셜사용자",
                    "010-123-456", // 잘못된 형식
                    true,
                    true,
                    false
            );

            // 2. 비즈니스 규칙 검증이 실패하도록 설정합니다.
            BusinessException businessException = new BusinessException("핸드폰 번호는 010-XXXX-XXXX 형식이어야 합니다.");
            willThrow(businessException).given(userValidationService).validateSocialSignupBusinessRules(
                    request.email(),
                    request.name(),
                    request
            );

            // when & then
            // 1. 핸드폰 번호 형식 오류로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.completeSocialSignup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("핸드폰 번호는 010-XXXX-XXXX 형식이어야 합니다.");

            // 2. 후속 처리가 수행되지 않았는지 확인합니다.
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("사용자 저장 실패 시 소셜 회원가입 실패 테스트")
        void shouldFailWhenUserSaveFails() {
            // given
            // 1. 유효한 소셜 회원가입 요청 정보를 준비합니다.
            SocialSignupRequest request = new SocialSignupRequest(
                    "google",
                    "google_user_123",
                    "social@example.com",
                    "소셜사용자",
                    "010-1234-5678",
                    true,
                    true,
                    false
            );

            // 2. 비즈니스 규칙 검증이 통과하도록 설정합니다.
            willDoNothing().given(userValidationService).validateSocialSignupBusinessRules(
                    request.email(),
                    request.name(),
                    request
            );

            // 3. 사용자 저장이 실패하도록 설정합니다.
            RuntimeException saveException = new RuntimeException("Database connection error");
            given(userRepository.save(any(User.class))).willThrow(saveException);

            // when & then
            // 1. 사용자 저장 실패로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.completeSocialSignup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("사용자 계정 생성에 실패했습니다.");

            // 2. 예상되는 메서드 호출만 검증합니다.
            then(userValidationService).should(times(1)).validateSocialSignupBusinessRules(
                    request.email(),
                    request.name(),
                    request
            );
            then(userRepository).should(times(1)).save(any(User.class));

            // 3. JWT 토큰 생성은 사용자 저장 후에 실행되므로 호출되지 않습니다.
            then(jwtConfig).should(never()).generateAccessToken(anyString(), any());
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class TokenRefreshTest {

        @Test
        @DisplayName("유효한 Refresh Token으로 토큰 갱신 성공 테스트")
        void shouldRefreshTokensSuccessfullyWithValidRefreshToken() {
            // given
            // 1. Authorization 헤더와 클라이언트 IP를 준비합니다.
            String authorizationHeader = "Bearer valid_refresh_token_123";
            String clientIp = "192.168.1.1";
            String refreshToken = "valid_refresh_token_123";

            // 2. JWT 설정에서 토큰 추출이 성공하도록 설정합니다.
            given(jwtConfig.extractTokenFromHeader(authorizationHeader)).willReturn(refreshToken);

            // 3. Refresh Token Service에서 토큰 갱신이 성공하도록 설정합니다.
            RefreshTokenService.TokenPair tokenPair = new RefreshTokenService.TokenPair(
                    "new_access_token_456",
                    "new_refresh_token_789"
            );
            given(refreshTokenService.refreshTokens(refreshToken, clientIp)).willReturn(tokenPair);

            // 4. Access Token 유효 시간을 설정합니다.
            given(jwtConfig.getAccessTokenValiditySeconds()).willReturn(900L); // 15분

            // when
            // 1. 토큰 갱신을 수행합니다.
            TokenRefreshResponse response = authService.refreshTokens(authorizationHeader, clientIp);

            // then
            // 1. 응답이 올바른지 확인합니다.
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("new_access_token_456");
            assertThat(response.refreshToken()).isEqualTo("new_refresh_token_789");
            assertThat(response.expiresIn()).isEqualTo(900L);

            // 2. JWT 설정에서 토큰 추출이 호출되었는지 확인합니다.
            then(jwtConfig).should(times(1)).extractTokenFromHeader(authorizationHeader);

            // 3. Refresh Token Service에서 토큰 갱신이 호출되었는지 확인합니다.
            then(refreshTokenService).should(times(1)).refreshTokens(refreshToken, clientIp);

            // 4. Access Token 유효 시간 조회가 호출되었는지 확인합니다.
            then(jwtConfig).should(times(1)).getAccessTokenValiditySeconds();
        }

        @Test
        @DisplayName("Authorization 헤더가 없을 때 토큰 갱신 실패 테스트")
        void shouldFailWhenAuthorizationHeaderIsNull() {
            // given
            // 1. null Authorization 헤더와 클라이언트 IP를 준비합니다.
            String authorizationHeader = null;
            String clientIp = "192.168.1.1";

            // 2. JWT 설정에서 토큰 추출이 null을 반환하도록 설정합니다.
            given(jwtConfig.extractTokenFromHeader(authorizationHeader)).willReturn(null);

            // when & then
            // 1. Authorization 헤더가 없어서 토큰 갱신 실패 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.refreshTokens(authorizationHeader, clientIp))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Authorization 헤더가 필요합니다.");

            // 2. JWT 설정에서 토큰 추출은 호출되지만 Refresh Token Service는 호출되지 않았는지 확인합니다.
            then(jwtConfig).should(times(1)).extractTokenFromHeader(authorizationHeader);
            then(refreshTokenService).should(never()).refreshTokens(anyString(), anyString());
        }

        @Test
        @DisplayName("잘못된 Refresh Token으로 토큰 갱신 실패 테스트")
        void shouldFailWhenRefreshTokenIsInvalid() {
            // given
            // 1. 잘못된 Refresh Token을 포함한 Authorization 헤더와 클라이언트 IP를 준비합니다.
            String authorizationHeader = "Bearer invalid_refresh_token";
            String clientIp = "192.168.1.1";
            String refreshToken = "invalid_refresh_token";

            // 2. JWT 설정에서 토큰 추출이 성공하도록 설정합니다.
            given(jwtConfig.extractTokenFromHeader(authorizationHeader)).willReturn(refreshToken);

            // 3. Refresh Token Service에서 토큰 갱신이 실패하도록 설정합니다.
            AuthenticationException authException = new AuthenticationException("유효하지 않은 리프레시 토큰입니다.");
            given(refreshTokenService.refreshTokens(refreshToken, clientIp)).willThrow(authException);

            // when & then
            // 1. 잘못된 Refresh Token으로 인한 토큰 갱신 실패 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.refreshTokens(authorizationHeader, clientIp))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("유효하지 않은 리프레시 토큰입니다.");

            // 2. 예상되는 메서드 호출이 수행되었는지 확인합니다.
            then(jwtConfig).should(times(1)).extractTokenFromHeader(authorizationHeader);
            then(refreshTokenService).should(times(1)).refreshTokens(refreshToken, clientIp);

            // 3. Access Token 유효 시간 조회는 호출되지 않았는지 확인합니다.
            then(jwtConfig).should(never()).getAccessTokenValiditySeconds();
        }

        @Test
        @DisplayName("만료된 Refresh Token으로 토큰 갱신 실패 테스트")
        void shouldFailWhenRefreshTokenIsExpired() {
            // given
            // 1. 만료된 Refresh Token을 포함한 Authorization 헤더와 클라이언트 IP를 준비합니다.
            String authorizationHeader = "Bearer expired_refresh_token";
            String clientIp = "192.168.1.1";
            String refreshToken = "expired_refresh_token";

            // 2. JWT 설정에서 토큰 추출이 성공하도록 설정합니다.
            given(jwtConfig.extractTokenFromHeader(authorizationHeader)).willReturn(refreshToken);

            // 3. Refresh Token Service에서 만료된 토큰 예외가 발생하도록 설정합니다.
            AuthenticationException expiredTokenException = new AuthenticationException("만료되었거나 유효하지 않은 리프레시 토큰입니다.");
            given(refreshTokenService.refreshTokens(refreshToken, clientIp)).willThrow(expiredTokenException);

            // when & then
            // 1. 만료된 Refresh Token으로 인한 토큰 갱신 실패 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> authService.refreshTokens(authorizationHeader, clientIp))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("만료되었거나 유효하지 않은 리프레시 토큰입니다.");

            // 2. 예상되는 메서드 호출이 수행되었는지 확인합니다.
            then(jwtConfig).should(times(1)).extractTokenFromHeader(authorizationHeader);
            then(refreshTokenService).should(times(1)).refreshTokens(refreshToken, clientIp);

            // 3. Access Token 유효 시간 조회는 호출되지 않았는지 확인합니다.
            then(jwtConfig).should(never()).getAccessTokenValiditySeconds();
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("유효한 토큰들로 로그아웃 성공 테스트")
        void shouldLogoutSuccessfullyWithValidTokens() {
            // given
            // 1. 유효한 Access Token과 Refresh Token, 클라이언트 IP를 준비합니다.
            String accessToken = "valid_access_token_123";
            String refreshToken = "valid_refresh_token_456";
            String clientIp = "192.168.1.1";

            // 2. Access Token 검증이 성공하도록 설정합니다.
            given(jwtConfig.validateToken(accessToken)).willReturn(true);
            given(jwtConfig.isAccessToken(accessToken)).willReturn(true);

            // 3. Refresh Token 검증이 성공하도록 설정합니다.
            given(jwtConfig.validateToken(refreshToken)).willReturn(true);
            given(jwtConfig.isRefreshToken(refreshToken)).willReturn(true);

            // 4. 블랙리스트 서비스와 Refresh Token 서비스가 성공하도록 설정합니다.
            willDoNothing().given(blacklistService).addAccessTokenToBlacklist(accessToken);
            willDoNothing().given(refreshTokenService).revokeRefreshToken(refreshToken);

            // when
            // 1. 로그아웃을 수행합니다.
            authService.logout(accessToken, refreshToken, clientIp);

            // then
            // 1. Access Token 검증이 호출되었는지 확인합니다.
            then(jwtConfig).should(times(1)).validateToken(accessToken);
            then(jwtConfig).should(times(1)).isAccessToken(accessToken);

            // 2. Refresh Token 검증이 호출되었는지 확인합니다.
            then(jwtConfig).should(times(1)).validateToken(refreshToken);
            then(jwtConfig).should(times(1)).isRefreshToken(refreshToken);

            // 3. 블랙리스트에 Access Token이 추가되었는지 확인합니다.
            then(blacklistService).should(times(1)).addAccessTokenToBlacklist(accessToken);

            // 4. Refresh Token이 무효화되었는지 확인합니다.
            then(refreshTokenService).should(times(1)).revokeRefreshToken(refreshToken);
        }

        @Test
        @DisplayName("Access Token만 있을 때 로그아웃 성공 테스트")
        void shouldLogoutSuccessfullyWithOnlyAccessToken() {
            // given
            // 1. Access Token만 있고 Refresh Token은 null인 경우를 준비합니다.
            String accessToken = "valid_access_token_123";
            String refreshToken = null;
            String clientIp = "192.168.1.1";

            // 2. Access Token 검증이 성공하도록 설정합니다.
            given(jwtConfig.validateToken(accessToken)).willReturn(true);
            given(jwtConfig.isAccessToken(accessToken)).willReturn(true);

            // 3. 블랙리스트 서비스가 성공하도록 설정합니다.
            willDoNothing().given(blacklistService).addAccessTokenToBlacklist(accessToken);

            // when
            // 1. 로그아웃을 수행합니다.
            authService.logout(accessToken, refreshToken, clientIp);

            // then
            // 1. Access Token 검증이 호출되었는지 확인합니다.
            then(jwtConfig).should(times(1)).validateToken(accessToken);
            then(jwtConfig).should(times(1)).isAccessToken(accessToken);

            // 2. 블랙리스트에 Access Token이 추가되었는지 확인합니다.
            then(blacklistService).should(times(1)).addAccessTokenToBlacklist(accessToken);

            // 3. Refresh Token 관련 처리는 호출되지 않았는지 확인합니다.
            then(refreshTokenService).should(never()).revokeRefreshToken(anyString());
        }

        @Test
        @DisplayName("Refresh Token만 있을 때 로그아웃 성공 테스트")
        void shouldLogoutSuccessfullyWithOnlyRefreshToken() {
            // given
            // 1. Refresh Token만 있고 Access Token은 null인 경우를 준비합니다.
            String accessToken = null;
            String refreshToken = "valid_refresh_token_456";
            String clientIp = "192.168.1.1";

            // 2. Refresh Token 검증이 성공하도록 설정합니다.
            given(jwtConfig.validateToken(refreshToken)).willReturn(true);
            given(jwtConfig.isRefreshToken(refreshToken)).willReturn(true);

            // 3. Refresh Token 서비스가 성공하도록 설정합니다.
            willDoNothing().given(refreshTokenService).revokeRefreshToken(refreshToken);

            // when
            // 1. 로그아웃을 수행합니다.
            authService.logout(accessToken, refreshToken, clientIp);

            // then
            // 1. Refresh Token 검증이 호출되었는지 확인합니다.
            then(jwtConfig).should(times(1)).validateToken(refreshToken);
            then(jwtConfig).should(times(1)).isRefreshToken(refreshToken);

            // 2. Refresh Token이 무효화되었는지 확인합니다.
            then(refreshTokenService).should(times(1)).revokeRefreshToken(refreshToken);

            // 3. Access Token 관련 처리는 호출되지 않았는지 확인합니다.
            then(blacklistService).should(never()).addAccessTokenToBlacklist(anyString());
        }

        @Test
        @DisplayName("유효하지 않은 Access Token으로 로그아웃 시 부분적 성공 테스트")
        void shouldLogoutPartiallyWhenAccessTokenIsInvalid() {
            // given
            // 1. 유효하지 않은 Access Token과 유효한 Refresh Token을 준비합니다.
            String accessToken = "invalid_access_token";
            String refreshToken = "valid_refresh_token_456";
            String clientIp = "192.168.1.1";

            // 2. Access Token 검증이 실패하도록 설정합니다.
            given(jwtConfig.validateToken(accessToken)).willReturn(false);

            // 3. Refresh Token 검증이 성공하도록 설정합니다.
            given(jwtConfig.validateToken(refreshToken)).willReturn(true);
            given(jwtConfig.isRefreshToken(refreshToken)).willReturn(true);

            // 4. Refresh Token 서비스가 성공하도록 설정합니다.
            willDoNothing().given(refreshTokenService).revokeRefreshToken(refreshToken);

            // when
            // 1. 로그아웃을 수행합니다.
            authService.logout(accessToken, refreshToken, clientIp);

            // then
            // 1. Access Token 검증이 호출되었지만 블랙리스트에는 추가되지 않았는지 확인합니다.
            then(jwtConfig).should(times(1)).validateToken(accessToken);
            then(blacklistService).should(never()).addAccessTokenToBlacklist(accessToken);

            // 2. Refresh Token은 정상적으로 무효화되었는지 확인합니다.
            then(jwtConfig).should(times(1)).validateToken(refreshToken);
            then(jwtConfig).should(times(1)).isRefreshToken(refreshToken);
            then(refreshTokenService).should(times(1)).revokeRefreshToken(refreshToken);
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token으로 로그아웃 시 부분적 성공 테스트")
        void shouldLogoutPartiallyWhenRefreshTokenIsInvalid() {
            // given
            // 1. 유효한 Access Token과 유효하지 않은 Refresh Token을 준비합니다.
            String accessToken = "valid_access_token_123";
            String refreshToken = "invalid_refresh_token";
            String clientIp = "192.168.1.1";

            // 2. Access Token 검증이 성공하도록 설정합니다.
            given(jwtConfig.validateToken(accessToken)).willReturn(true);
            given(jwtConfig.isAccessToken(accessToken)).willReturn(true);

            // 3. Refresh Token 검증이 실패하도록 설정합니다.
            given(jwtConfig.validateToken(refreshToken)).willReturn(false);

            // 4. 블랙리스트 서비스가 성공하도록 설정합니다.
            willDoNothing().given(blacklistService).addAccessTokenToBlacklist(accessToken);

            // when
            // 1. 로그아웃을 수행합니다.
            authService.logout(accessToken, refreshToken, clientIp);

            // then
            // 1. Access Token은 정상적으로 블랙리스트에 추가되었는지 확인합니다.
            then(jwtConfig).should(times(1)).validateToken(accessToken);
            then(jwtConfig).should(times(1)).isAccessToken(accessToken);
            then(blacklistService).should(times(1)).addAccessTokenToBlacklist(accessToken);

            // 2. Refresh Token 검증이 호출되었지만 무효화는 되지 않았는지 확인합니다.
            then(jwtConfig).should(times(1)).validateToken(refreshToken);
            then(refreshTokenService).should(never()).revokeRefreshToken(refreshToken);
        }

        @Test
        @DisplayName("토큰들이 모두 null일 때 로그아웃 성공 테스트")
        void shouldLogoutSuccessfullyWhenAllTokensAreNull() {
            // given
            // 1. 모든 토큰이 null인 경우를 준비합니다.
            String accessToken = null;
            String refreshToken = null;
            String clientIp = "192.168.1.1";

            // when
            // 1. 로그아웃을 수행합니다.
            authService.logout(accessToken, refreshToken, clientIp);

            // then
            // 1. 토큰 검증이나 처리가 호출되지 않았는지 확인합니다.
            then(jwtConfig).should(never()).validateToken(anyString());
            then(blacklistService).should(never()).addAccessTokenToBlacklist(anyString());
            then(refreshTokenService).should(never()).revokeRefreshToken(anyString());
        }

        @Test
        @DisplayName("블랙리스트 서비스 예외 발생 시 로그아웃 부분적 성공 테스트")
        void shouldLogoutPartiallyWhenBlacklistServiceFails() {
            // given
            // 1. 유효한 Access Token과 Refresh Token을 준비합니다.
            String accessToken = "valid_access_token_123";
            String refreshToken = "valid_refresh_token_456";
            String clientIp = "192.168.1.1";

            // 2. Access Token 검증이 성공하도록 설정합니다.
            given(jwtConfig.validateToken(accessToken)).willReturn(true);
            given(jwtConfig.isAccessToken(accessToken)).willReturn(true);

            // 3. 블랙리스트 서비스에서 예외가 발생하도록 설정합니다.
            RuntimeException blacklistException = new RuntimeException("Blacklist service error");
            willThrow(blacklistException).given(blacklistService).addAccessTokenToBlacklist(accessToken);

            // when & then
            // 1. 로그아웃이 예외 없이 완료되는지 확인합니다 (부분적 실패라도 성공으로 처리).
            authService.logout(accessToken, refreshToken, clientIp);

            // 2. Access Token 관련 처리가 수행되었는지 확인합니다.
            then(jwtConfig).should(times(1)).validateToken(accessToken);
            then(jwtConfig).should(times(1)).isAccessToken(accessToken);
            then(blacklistService).should(times(1)).addAccessTokenToBlacklist(accessToken);

            // 3. 실제 구현체에서는 예외가 발생하면 try-catch로 인해 Refresh Token 처리가 실행되지 않습니다.
            // 따라서 Refresh Token 관련 메서드들이 호출되지 않았는지 확인합니다.
            then(jwtConfig).should(never()).validateToken(refreshToken);
            then(jwtConfig).should(never()).isRefreshToken(refreshToken);
            then(refreshTokenService).should(never()).revokeRefreshToken(refreshToken);
        }
    }
}