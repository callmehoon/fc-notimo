package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private VerificationStorage verificationStorage;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Nested
    @DisplayName("Rate Limit과 함께 인증 코드 발송 테스트")
    class SendVerificationCodeWithRateLimitTest {

        @Test
        @DisplayName("Rate Limit 통과 후 인증 코드 발송 성공 테스트")
        void shouldSendVerificationCodeWhenRateLimitPassed() {
            // given
            // 1. 테스트용 이메일과 클라이언트 IP를 준비합니다.
            String email = "test@example.com";
            String clientIp = "192.168.1.1";
            // 2. Rate limit이 통과하도록 설정합니다.
            willDoNothing().given(rateLimitService).checkEmailSendRateLimit(clientIp, email);

            // when
            // 1. Rate limit과 함께 인증 코드를 발송합니다.
            emailService.sendVerificationCodeWithRateLimit(email, clientIp);

            // then
            // 1. Rate limit 검사가 수행되었는지 확인합니다.
            then(rateLimitService).should(times(1)).checkEmailSendRateLimit(clientIp, email);
            // 2. 메일이 발송되었는지 확인합니다.
            then(mailSender).should(times(1)).send(any(SimpleMailMessage.class));
            // 3. 인증 코드가 저장소에 저장되었는지 확인합니다.
            then(verificationStorage).should(times(1)).save(eq(email), anyString());
        }

        @Test
        @DisplayName("Rate Limit 초과 시 인증 코드 발송 실패 테스트")
        void shouldFailWhenRateLimitExceeded() {
            // given
            // 1. 테스트용 이메일과 클라이언트 IP를 준비합니다.
            String email = "test@example.com";
            String clientIp = "192.168.1.1";
            // 2. Rate limit 초과 예외가 발생하도록 설정합니다.
            RuntimeException rateLimitException = new RuntimeException("Rate limit exceeded");
            willThrow(rateLimitException).given(rateLimitService).checkEmailSendRateLimit(clientIp, email);

            // when & then
            // 1. Rate limit 초과로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> emailService.sendVerificationCodeWithRateLimit(email, clientIp))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Rate limit exceeded");

            // 2. Rate limit 검사가 수행되었는지 확인합니다.
            then(rateLimitService).should(times(1)).checkEmailSendRateLimit(clientIp, email);
            // 3. 메일 발송은 시도되지 않았는지 확인합니다.
            then(mailSender).should(never()).send(any(SimpleMailMessage.class));
        }
    }

    @Nested
    @DisplayName("인증 코드 발송 테스트")
    class SendVerificationCodeTest {

        @Test
        @DisplayName("유효한 이메일로 인증 코드 발송 성공 테스트")
        void shouldSendVerificationCodeWhenValidEmail() {
            // given
            // 1. 유효한 이메일을 준비합니다.
            String email = "test@example.com";
            // 2. 메일 발송이 성공하도록 설정합니다.
            willDoNothing().given(mailSender).send(any(SimpleMailMessage.class));

            // when
            // 1. 인증 코드를 발송합니다.
            emailService.sendVerificationCode(email);

            // then
            // 1. 메일이 발송되었는지 확인합니다.
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            then(mailSender).should(times(1)).send(messageCaptor.capture());

            // 2. 발송된 메일의 내용이 올바른지 확인합니다.
            SimpleMailMessage sentMessage = messageCaptor.getValue();
            assertThat(sentMessage.getTo()).containsExactly(email);
            assertThat(sentMessage.getSubject()).isEqualTo("[notimo] 회원가입 이메일 인증 코드입니다.");
            assertThat(sentMessage.getText()).contains("인증 코드:");
            assertThat(sentMessage.getText()).contains("5분 후에 만료됩니다");

            // 3. 인증 코드가 저장소에 저장되었는지 확인합니다.
            then(verificationStorage).should(times(1)).save(eq(email), anyString());
        }

        @Test
        @DisplayName("빈 이메일로 인증 코드 발송 실패 테스트")
        void shouldFailWhenEmailIsEmpty() {
            // given
            // 1. 빈 이메일을 준비합니다.
            String emptyEmail = "";

            // when & then
            // 1. 빈 이메일로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> emailService.sendVerificationCode(emptyEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일을 입력해주세요.");

            // 2. 메일 발송은 시도되지 않았는지 확인합니다.
            then(mailSender).should(never()).send(any(SimpleMailMessage.class));
            // 3. 저장소에 저장되지 않았는지 확인합니다.
            then(verificationStorage).should(never()).save(anyString(), anyString());
        }

        @Test
        @DisplayName("null 이메일로 인증 코드 발송 실패 테스트")
        void shouldFailWhenEmailIsNull() {
            // given
            // 1. null 이메일을 준비합니다.
            String nullEmail = null;

            // when & then
            // 1. null 이메일로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> emailService.sendVerificationCode(nullEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일을 입력해주세요.");

            // 2. 메일 발송은 시도되지 않았는지 확인합니다.
            then(mailSender).should(never()).send(any(SimpleMailMessage.class));
            // 3. 저장소에 저장되지 않았는지 확인합니다.
            then(verificationStorage).should(never()).save(anyString(), anyString());
        }

        @Test
        @DisplayName("메일 발송 중 예외 발생 시 처리 테스트")
        void shouldHandleMailSendingException() {
            // given
            // 1. 유효한 이메일을 준비합니다.
            String email = "test@example.com";
            // 2. 메일 발송 시 예외가 발생하도록 설정합니다.
            RuntimeException mailException = new RuntimeException("Mail server error");
            willThrow(mailException).given(mailSender).send(any(SimpleMailMessage.class));

            // when & then
            // 1. 메일 발송 실패로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> emailService.sendVerificationCode(email))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("인증 코드 전송에 실패했습니다. 다시 시도해주세요.")
                    .hasCause(mailException);

            // 2. 메일 발송이 시도되었는지 확인합니다.
            then(mailSender).should(times(1)).send(any(SimpleMailMessage.class));
            // 3. 발송 실패로 인해 저장소에 저장되지 않았는지 확인합니다.
            then(verificationStorage).should(never()).save(anyString(), anyString());
        }

        @Test
        @DisplayName("MailSender가 null일 때 개발환경 처리 테스트")
        void shouldHandleNullMailSenderInDevelopment() {
            // given
            // 1. MailSender가 null인 EmailService를 생성합니다.
            EmailService emailServiceWithNullSender = new EmailService(verificationStorage, rateLimitService, null);
            String email = "test@example.com";

            // when
            // 1. MailSender가 null 상태에서 인증 코드를 발송합니다.
            emailServiceWithNullSender.sendVerificationCode(email);

            // then
            // 1. 인증 코드가 저장소에 저장되었는지 확인합니다. (개발환경에서는 저장만 수행)
            then(verificationStorage).should(times(1)).save(eq(email), anyString());
        }
    }
}