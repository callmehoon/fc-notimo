package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final VerificationStorage verificationStorage;
    private final RateLimitService rateLimitService;
    
    public EmailService(VerificationStorage verificationStorage,
                       RateLimitService rateLimitService,
                       @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {
        this.verificationStorage = verificationStorage;
        this.rateLimitService = rateLimitService;
        this.mailSender = mailSender;
    }
    
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;

    /**
     * Rate limiting과 함께 인증 코드 발송
     * 기본값으로 회원가입 용도로 발송
     */
    public void sendVerificationCodeWithRateLimit(String email, String clientIp) {
        sendVerificationCodeWithRateLimit(email, clientIp, EmailPurpose.SIGNUP);
    }

    /**
     * Rate limiting과 함께 특정 용도의 인증 코드 발송
     *
     * @param email 수신자 이메일
     * @param clientIp 클라이언트 IP
     * @param purpose 이메일 용도
     */
    public void sendVerificationCodeWithRateLimit(String email, String clientIp, EmailPurpose purpose) {
        // Rate limiting 체크
        rateLimitService.checkEmailSendRateLimit(clientIp, email);

        // 이메일 발송 로직 호출
        sendVerificationCode(email, purpose);
    }

    /**
     * 기본값으로 회원가입 용도의 인증 코드 발송
     */
    public void sendVerificationCode(String email) {
        sendVerificationCode(email, EmailPurpose.SIGNUP);
    }

    /**
     * 특정 용도의 인증 코드 발송
     *
     * @param email 수신자 이메일
     * @param purpose 이메일 용도
     */
    public void sendVerificationCode(String email, EmailPurpose purpose) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        log.info("인증 코드 전송 시작: email={}, purpose={}", email, purpose);

        try {
            String code = createRandomCode();

            if (mailSender != null) {
                // 실제 이메일 발송
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject(purpose.getEmailSubject());
                message.setText(purpose.getEmailContent(code, CODE_EXPIRY_MINUTES));

                mailSender.send(message);
                log.info("인증 코드 전송 성공: email={}, purpose={}", email, purpose);

                // 이메일 발송 성공 시에만 인증 코드 저장
                verificationStorage.save(email, code);
            } else {
                // 개발환경에서는 로그로만 기록하고 인증 코드 저장
                log.warn("이메일 발송 비활성화 상태 - 인증 코드 로그 출력: email={}, purpose={}, code={}",
                        email, purpose, code);
                verificationStorage.save(email, code);
            }

        } catch (Exception e) {
            log.error("인증 코드 전송 실패: email={}, purpose={}, error={}", email, purpose, e.getMessage());
            throw new RuntimeException("인증 코드 전송에 실패했습니다. 다시 시도해주세요.", e);
        }
    }

    private String createRandomCode() {
        SecureRandom random = new SecureRandom();
        int min = (int) Math.pow(10, CODE_LENGTH - 1);
        int max = (int) Math.pow(10, CODE_LENGTH) - 1;
        return String.valueOf(min + random.nextInt(max - min + 1));
    }
}