package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.exception.RateLimitExceededException;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private VerificationStorage verificationStorage;

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("성공: 유효한 이메일로 인증 코드 발송")
    void sendVerificationCode_success() {
        // given
        String email = "test@example.com";

        // when
        emailService.sendVerificationCode(email);

        // then
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(verificationStorage).save(eq(email), anyString());
    }

    @Test
    @DisplayName("실패: 빈 이메일로 인증 코드 발송 시 예외 발생")
    void sendVerificationCode_fail_emptyEmail() {
        // given
        String emptyEmail = "";

        // when & then
        assertThatThrownBy(() -> emailService.sendVerificationCode(emptyEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일을 입력해주세요.");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(verificationStorage, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("성공: Rate limiting 통과 시 인증 코드 발송")
    void sendVerificationCodeWithRateLimit_success() {
        // given
        String email = "test@example.com";
        String clientIp = "192.168.1.1";
        when(rateLimitService.isEmailSendAllowed(clientIp)).thenReturn(true);

        // when
        emailService.sendVerificationCodeWithRateLimit(email, clientIp);

        // then
        verify(rateLimitService).isEmailSendAllowed(clientIp);
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(verificationStorage).save(eq(email), anyString());
    }

    @Test
    @DisplayName("실패: Rate limiting 초과 시 예외 발생")
    void sendVerificationCodeWithRateLimit_fail_rateLimitExceeded() {
        // given
        String email = "test@example.com";
        String clientIp = "192.168.1.1";
        long waitTime = 300L;
        
        when(rateLimitService.isEmailSendAllowed(clientIp)).thenReturn(false);
        when(rateLimitService.getEmailSendWaitTime(clientIp)).thenReturn(waitTime);

        // when & then
        assertThatThrownBy(() -> emailService.sendVerificationCodeWithRateLimit(email, clientIp))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("이메일 발송 속도 제한을 초과했습니다. 300초 후 다시 시도해주세요.");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(verificationStorage, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("성공: 인증 코드는 6자리 숫자여야 함")
    void createRandomCode_shouldBe6Digits() {
        // given
        String email = "test@example.com";

        // when
        emailService.sendVerificationCode(email);

        // then
        verify(verificationStorage).save(eq(email), argThat(code -> 
            code.length() == 6 && code.matches("\\d{6}")
        ));
    }
}
