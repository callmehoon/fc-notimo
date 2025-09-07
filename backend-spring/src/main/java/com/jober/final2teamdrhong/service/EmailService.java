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
     */
    public void sendVerificationCodeWithRateLimit(String email, String clientIp) {
        // Rate limiting 체크
        rateLimitService.checkEmailSendRateLimit(clientIp, email);
        
        // 기존 이메일 발송 로직 호출
        sendVerificationCode(email);
    }

    public void sendVerificationCode(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
        
        log.info("인증 코드 전송 시작: email={}", email);
        
        try {
            String code = createRandomCode();

            if (mailSender != null) {
                // 실제 이메일 발송
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("[notimo] 회원가입 이메일 인증 코드입니다.");
                message.setText(createEmailContent(code));
                
                mailSender.send(message);
                log.info("인증 코드 전송 성공: email={}", email);
                
                // 이메일 발송 성공 시에만 인증 코드 저장
                verificationStorage.save(email, code);
            } else {
                // 개발환경에서는 로그로만 기록하고 인증 코드 저장
                log.warn("이메일 발송 비활성화 상태 - 인증 코드 로그 출력: email={}, code={}", email, code);
                verificationStorage.save(email, code);
            }
            
        } catch (Exception e) {
            log.error("인증 코드 전송 실패: email={}, error={}", email, e.getMessage());
            throw new RuntimeException("인증 코드 전송에 실패했습니다. 다시 시도해주세요.", e);
        }
    }

    private String createRandomCode() {
        SecureRandom random = new SecureRandom();
        int min = (int) Math.pow(10, CODE_LENGTH - 1);
        int max = (int) Math.pow(10, CODE_LENGTH) - 1;
        return String.valueOf(min + random.nextInt(max - min + 1));
    }
    
    private String createEmailContent(String code) {
        return String.format(
            "회원가입을 위해 아래 인증 코드를 입력해주세요.\n\n" +
            "인증 코드: %s\n\n" +
            "이 코드는 %d분 후에 만료됩니다.\n" +
            "만약 본인이 요청하지 않았다면 이 메일을 무시해주세요.",
            code, CODE_EXPIRY_MINUTES
        );
    }
}