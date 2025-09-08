package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.UserSignupRequest;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 보안 기능 테스트")
class UserServiceSecurityTest {

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
    @DisplayName("constantTimeEquals: 동일한 문자열 비교 시 true 반환")
    void constantTimeEquals_sameStrings_returnsTrue() throws Exception {
        // given
        String str1 = "123456";
        String str2 = "123456";
        
        // when
        boolean result = invokeConstantTimeEquals(str1, str2);
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("constantTimeEquals: 다른 문자열 비교 시 false 반환")
    void constantTimeEquals_differentStrings_returnsFalse() throws Exception {
        // given
        String str1 = "123456";
        String str2 = "654321";
        
        // when
        boolean result = invokeConstantTimeEquals(str1, str2);
        
        // then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("constantTimeEquals: 길이가 다른 문자열 비교 시 false 반환")
    void constantTimeEquals_differentLengthStrings_returnsFalse() throws Exception {
        // given
        String str1 = "123456";
        String str2 = "12345";
        
        // when
        boolean result = invokeConstantTimeEquals(str1, str2);
        
        // then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("constantTimeEquals: null 값 처리")
    void constantTimeEquals_nullValues_handledCorrectly() throws Exception {
        // given & when & then
        assertThat(invokeConstantTimeEquals(null, null)).isTrue();
        assertThat(invokeConstantTimeEquals(null, "123456")).isFalse();
        assertThat(invokeConstantTimeEquals("123456", null)).isFalse();
    }
    
    @Test
    @DisplayName("타이밍 공격 방지: 틀린 인증 코드로 회원가입 시도")
    void timingAttackPrevention_wrongVerificationCode() {
        // given
        String email = "test@example.com";
        String correctCode = "123456";
        String wrongCode = "000000";
        
        UserSignupRequest requestDto = UserSignupRequest.builder()
                .userName("홍길동")
                .email(email)
                .password("Test123!")
                .userNumber("12345678")
                .verificationCode(wrongCode)
                .build();
        
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.empty());
        when(rateLimitService.isEmailVerifyAllowed(email)).thenReturn(true);
        when(verificationStorage.find(email)).thenReturn(Optional.of(correctCode));
        
        // when & then
        assertThatThrownBy(() -> userService.signup(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증 코드가 일치하지 않습니다.");
        
        // verify
        verify(verificationStorage, times(1)).find(email);
        verify(userRepository, never()).save(any(User.class));
    }
    
    /**
     * 리플렉션을 사용하여 private constantTimeEquals 메서드 테스트
     */
    private boolean invokeConstantTimeEquals(String a, String b) throws Exception {
        Method method = UserService.class.getDeclaredMethod("constantTimeEquals", String.class, String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(userService, a, b);
    }
}