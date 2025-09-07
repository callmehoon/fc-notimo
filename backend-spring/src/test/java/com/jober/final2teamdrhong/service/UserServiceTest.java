package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.UserSignupRequestDto;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.exception.RateLimitExceededException;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private VerificationStorage verificationStorage;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("성공: 유효한 데이터로 회원가입")
    void signup_success() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();

        when(verificationStorage.find("test@example.com")).thenReturn(Optional.of("123456"));
        when(rateLimitService.isEmailVerifyAllowed("test@example.com")).thenReturn(true);
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Test123!")).thenReturn("encoded_password");

        // when
        userService.signup(requestDto);

        // then
        verify(verificationStorage).find("test@example.com");
        verify(userRepository).findByUserEmail("test@example.com");
        verify(passwordEncoder).encode("Test123!");
        verify(userRepository).save(any(User.class));
        verify(verificationStorage).delete("test@example.com");
    }

    @Test
    @DisplayName("실패: 중복된 이메일이면 예외 발생")
    void signup_fail_duplicateEmail() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();

        // 이미 가입된 사용자가 존재 - validateBusinessRules에서 먼저 체크됨
        User existingUser = User.create("기존유저", "test@example.com", "87654321");
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() -> userService.signup(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 가입된 이메일입니다.");

        verify(userRepository).findByUserEmail("test@example.com");
        verify(verificationStorage, never()).find(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("실패: 인증 코드가 일치하지 않으면 예외 발생")
    void signup_fail_invalidVerificationCode() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("999999")
                .build();

        // 비즈니스 규칙 먼저 체크 (중복 이메일 없음)
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.empty());
        // 인증 코드 검증
        when(rateLimitService.isEmailVerifyAllowed("test@example.com")).thenReturn(true);
        when(verificationStorage.find("test@example.com")).thenReturn(Optional.of("123456"));

        // when & then
        assertThatThrownBy(() -> userService.signup(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증 코드가 일치하지 않습니다.");

        verify(userRepository, never()).save(any());
        verify(verificationStorage, never()).delete(anyString());
    }

    @Test
    @DisplayName("실패: 인증 코드가 만료되었으면 예외 발생")
    void signup_fail_expiredVerificationCode() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();

        // 비즈니스 규칙 먼저 체크 (중복 이메일 없음)
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.empty());
        // 인증 코드 검증
        when(rateLimitService.isEmailVerifyAllowed("test@example.com")).thenReturn(true);
        when(verificationStorage.find("test@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.signup(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증 코드가 만료되었거나 유효하지 않습니다.");

        verify(userRepository, never()).save(any());
    }


    @Test
    @DisplayName("성공: Rate limiting 통과 시 회원가입")
    void signupWithRateLimit_success() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();
        String clientIp = "192.168.1.1";

        when(rateLimitService.isSignupAllowed(clientIp)).thenReturn(true);
        when(verificationStorage.find("test@example.com")).thenReturn(Optional.of("123456"));
        when(rateLimitService.isEmailVerifyAllowed("test@example.com")).thenReturn(true);
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Test123!")).thenReturn("encoded_password");

        // when
        userService.signupWithRateLimit(requestDto, clientIp);

        // then
        verify(rateLimitService).isSignupAllowed(clientIp);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("실패: Rate limiting 초과 시 예외 발생")
    void signupWithRateLimit_fail_rateLimitExceeded() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();
        String clientIp = "192.168.1.1";
        long waitTime = 3600L;

        when(rateLimitService.isSignupAllowed(clientIp)).thenReturn(false);
        when(rateLimitService.getSignupWaitTime(clientIp)).thenReturn(waitTime);

        // when & then
        assertThatThrownBy(() -> userService.signupWithRateLimit(requestDto, clientIp))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("회원가입 속도 제한을 초과했습니다. 3600초 후 다시 시도해주세요.");

        verify(userRepository, never()).save(any());
    }
}
