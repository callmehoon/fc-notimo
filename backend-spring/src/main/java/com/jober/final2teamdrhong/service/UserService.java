package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.UserSignupRequest;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final VerificationStorage verificationStorage;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimitService;

    /**
     * Rate limiting과 함께 회원가입 처리
     */
    public void signupWithRateLimit(UserSignupRequest requestDto, String clientIp) {
        // Rate limiting 체크
        rateLimitService.checkSignupRateLimit(clientIp, requestDto.getEmail());
        
        // 기존 회원가입 로직 호출
        signup(requestDto);
    }

    public void signup(UserSignupRequest requestDto) {
        log.info("회원가입 시작: email={}", requestDto.getEmail());
        
        // 1. 비즈니스 규칙 검증 (기본 유효성 검증은 @Valid에서 처리됨)
        validateBusinessRules(requestDto);
        
        // 2. 인증 코드 검증
        validateVerificationCode(requestDto.getEmail(), requestDto.getVerificationCode());

        try {
            // 4. 새로운 User 생성
            User newUser = User.create(
                    requestDto.getUserName(),
                    requestDto.getEmail(),
                    requestDto.getUserNumber()
            );

            // 5. 비밀번호 암호화 및 UserAuth 생성
            String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
            UserAuth userAuth = UserAuth.builder()
                    .authType(UserAuth.AuthType.LOCAL)
                    .passwordHash(encodedPassword)
                    .build();

            // 6. 관계 설정 및 인증 상태 업데이트
            newUser.addUserAuth(userAuth);
            userAuth.markAsVerified(); // 이메일 인증을 완료했으므로 인증 완료 처리
            userRepository.save(newUser);
            
            log.info("회원가입 성공: userId={}, email={}", newUser.getUserId(), requestDto.getEmail());
            
            // 7. 회원가입 성공 후에만 인증 코드 삭제 (트랜잭션 외부에서 실행)
            deleteVerificationCodeAfterSuccess(requestDto.getEmail());
            
        } catch (Exception e) {
            log.error("회원가입 실패: email={}, error={}", requestDto.getEmail(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 회원가입 성공 후 인증 코드 삭제
     * 별도 트랜잭션으로 실행하여 회원가입 실패 시에도 인증 코드가 유지되도록 함
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void deleteVerificationCodeAfterSuccess(String email) {
        try {
            verificationStorage.delete(email);
            log.info("인증 코드 삭제 완료: email={}", email);
        } catch (Exception e) {
            // 인증 코드 삭제 실패는 회원가입에 영향주지 않음
            log.warn("인증 코드 삭제 실패 (회원가입은 성공): email={}, error={}", email, e.getMessage());
        }
    }
    
    private void validateBusinessRules(UserSignupRequest requestDto) {
        // 이메일 중복 확인 (비즈니스 규칙)
        if (userRepository.findByUserEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        
        // 추가 비즈니스 규칙들이 여기에 들어갈 수 있음
        // 예: 이메일 도메인 제한, 사용자명 금지어 체크 등
    }
    
    private void validateVerificationCode(String email, String inputCode) {
        // Rate limiting 검사: 이메일별 검증 실패 제한
        rateLimitService.checkEmailVerifyRateLimit(email);
        
        String savedCode = verificationStorage.find(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 코드가 만료되었거나 유효하지 않습니다."));
        
        if (!constantTimeEquals(savedCode, inputCode)) {
            log.warn("인증 코드 불일치: email={}", email);
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        
        log.info("인증 코드 검증 성공: email={}", email);
    }
    
    /**
     * 타이밍 공격을 방지하기 위한 상수 시간 문자열 비교
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }
}