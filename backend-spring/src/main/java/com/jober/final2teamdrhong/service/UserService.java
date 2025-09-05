package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.UserSignupRequestDto;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final VerificationStorage verificationStorage;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserSignupRequestDto requestDto) {
        log.info("회원가입 시작: email={}", requestDto.getEmail());
        
        try {
            // 1. 입력 유효성 검증
            validateSignupRequest(requestDto);
            
            // 2. 인증 코드 검증
            validateVerificationCode(requestDto.getEmail(), requestDto.getVerificationCode());

            // 3. 이메일 중복 확인
            if (userRepository.findByUserEmail(requestDto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("이미 가입된 이메일입니다.");
            }

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

            // 7. 사용된 인증 코드 삭제
            verificationStorage.delete(requestDto.getEmail());
            
            log.info("회원가입 성공: userId={}, email={}", newUser.getUserId(), requestDto.getEmail());
        } catch (Exception e) {
            log.error("회원가입 실패: email={}, error={}", requestDto.getEmail(), e.getMessage());
            throw e;
        }
    }
    
    private void validateSignupRequest(UserSignupRequestDto requestDto) {
        if (!StringUtils.hasText(requestDto.getUserName())) {
            throw new IllegalArgumentException("사용자 이름을 입력해주세요.");
        }
        if (!StringUtils.hasText(requestDto.getEmail())) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
        if (!StringUtils.hasText(requestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
        if (!StringUtils.hasText(requestDto.getVerificationCode())) {
            throw new IllegalArgumentException("인증 코드를 입력해주세요.");
        }
    }
    
    private void validateVerificationCode(String email, String inputCode) {
        String savedCode = verificationStorage.find(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 코드가 만료되었거나 유효하지 않습니다."));
        
        if (!savedCode.equals(inputCode)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
    }
}