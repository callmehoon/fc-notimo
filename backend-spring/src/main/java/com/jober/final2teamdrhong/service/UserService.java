package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.config.AuthProperties;
import com.jober.final2teamdrhong.dto.changePassword.PasswordResetRequest;
import com.jober.final2teamdrhong.dto.changePassword.ConfirmPasswordResetRequest;
import com.jober.final2teamdrhong.dto.user.DeleteUserRequest;
import com.jober.final2teamdrhong.dto.user.UserProfileResponse;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.exception.AuthenticationException;
import com.jober.final2teamdrhong.exception.BusinessException;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.service.storage.VerificationStorage;
import com.jober.final2teamdrhong.util.LogMaskingUtil;
import com.jober.final2teamdrhong.util.TimingAttackProtection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RateLimitService rateLimitService;
    private final VerificationStorage verificationStorage;
    private final TimingAttackProtection timingAttackProtection;
    private final AuthProperties authProperties;

    /**
     * 사용자 프로필 정보 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 프로필 정보
     */
    public UserProfileResponse getUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다."));

        return UserProfileResponse.from(user);
    }

    /**
     * 마이페이지에서 비밀번호 변경
     *
     * @param userId 사용자 ID
     * @param request 비밀번호 변경 요청
     * @param clientIp 클라이언트 IP (Rate Limit 초기화용)
     */
    public void changePassword(Integer userId, PasswordResetRequest request, String clientIp) {
        log.info("비밀번호 변경 시작: userId={}", userId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다."));

        // 2. 로컬 인증 정보 조회
        UserAuth userAuth = user.getUserAuths().stream()
                .filter(auth -> auth.getAuthType() == UserAuth.AuthType.LOCAL)
                .findFirst()
                .orElseThrow(() -> new BusinessException("로컬 인증 정보가 없습니다."));

        // 3. 현재 비밀번호 검증 (타이밍 공격 방지)
        timingAttackProtection.startTiming();

        try {
            boolean passwordMatches = passwordEncoder.matches(request.currentPassword(), userAuth.getPasswordHash());
            long delay = authProperties.getSecurity().getTimingAttackDelayMs();

            if (!passwordMatches) {
                timingAttackProtection.ensureMinimumResponseTime(delay);
                log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: userId={}", userId);
                throw new AuthenticationException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 성공 시에도 최소 시간 보장
            timingAttackProtection.ensureMinimumResponseTime(delay);

        } finally {
            timingAttackProtection.clear();
        }

        // 4. 새 비밀번호가 현재와 다른지 확인
        if (passwordEncoder.matches(request.newPassword(), userAuth.getPasswordHash())) {
            throw new BusinessException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 5. 비밀번호 변경
        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        userAuth.updatePasswordHash(newPasswordHash);

        // 6. 보안 처리 - 모든 토큰 무효화 (RefreshTokenService 활용)
        tokenService.addAllUserTokensToBlacklist(userId);
        rateLimitService.resetLoginRateLimit(user.getUserEmail(), clientIp);

        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    /**
     * 이메일 인증 후 비밀번호 재설정
     *
     * @param request 비밀번호 재설정 요청
     * @param clientIp 클라이언트 IP
     */
    public void resetPassword(ConfirmPasswordResetRequest request, String clientIp) {
        log.info("비밀번호 재설정 시작: email={}", LogMaskingUtil.maskEmail(request.email()));

        // 1. Rate Limit 체크
        rateLimitService.checkEmailVerifyRateLimit(request.email());

        // 2. 인증 코드 검증 (일회성)
        if (!verificationStorage.validateAndDelete(request.email(), request.verificationCode())) {
            log.warn("비밀번호 재설정 실패 - 인증 코드 불일치: email={}",
                    LogMaskingUtil.maskEmail(request.email()));
            throw new BusinessException("인증 코드가 일치하지 않거나 만료되었습니다.");
        }

        // 3. 사용자 조회
        User user = userRepository.findByUserEmail(request.email())
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다."));

        // 4. 로컬 인증 정보 조회
        UserAuth userAuth = user.getUserAuths().stream()
                .filter(auth -> auth.getAuthType() == UserAuth.AuthType.LOCAL)
                .findFirst()
                .orElseThrow(() -> new BusinessException("로컬 인증 정보가 없습니다."));

        // 5. 비밀번호 변경
        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        userAuth.updatePasswordHash(newPasswordHash);

        // 6. 보안 처리 - 모든 토큰 무효화 (RefreshTokenService 활용)
        tokenService.addAllUserTokensToBlacklist(user.getUserId());
        rateLimitService.resetLoginRateLimit(user.getUserEmail(), clientIp);

        log.info("비밀번호 재설정 완료: email={}", LogMaskingUtil.maskEmail(request.email()));
    }

    /**
     * 회원 탈퇴 처리
     * Soft Delete 방식으로 처리하며, 개인정보를 익명화합니다.
     *
     * @param userId 사용자 ID
     * @param request 탈퇴 요청 정보
     * @param clientIp 클라이언트 IP
     */
    public void deleteAccount(Integer userId, DeleteUserRequest request, String clientIp) {
        log.info("[ACCOUNT_DELETE] 회원 탈퇴 요청 시작: userId={}", userId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다."));

        // 2. 이미 탈퇴한 회원인지 확인
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            log.warn("[ACCOUNT_DELETE] 이미 탈퇴한 회원의 탈퇴 요청: userId={}", userId);
            throw new BusinessException("이미 탈퇴 처리된 계정입니다.");
        }

        // Rate Limit 재설정을 위해 원래 이메일 저장
        String originalEmail = user.getUserEmail();

        // 3. 로컬 인증 정보가 있는 경우 비밀번호 검증
        user.getUserAuths().stream()
                .filter(auth -> auth.getAuthType() == UserAuth.AuthType.LOCAL)
                .findFirst()
                .ifPresent(localAuth -> verifyPasswordForDeletion(localAuth, request.password(), userId));

        // 4. 익명화된 이메일 생성 (타임스탬프 기반)
        String anonymizedEmail = String.format(authProperties.getSecurity().getAnonymizedEmailFormat(),
                userId, System.currentTimeMillis());

        // 5. 회원 탈퇴 처리 (Soft Delete + 개인정보 익명화)
        user.deleteAccount(anonymizedEmail);

        // 6. 모든 토큰 무효화
        tokenService.addAllUserTokensToBlacklist(userId);

        // 7. Rate Limit 정보 초기화 (원래 이메일 사용)
        rateLimitService.resetLoginRateLimit(originalEmail, clientIp);

        // 8. Redis 세션 정보 삭제 (있는 경우)
        // 향후 필요시 추가 구현

        log.info("[ACCOUNT_DELETE] 회원 탈퇴 완료: userId={}, anonymizedEmail={}",
                userId, anonymizedEmail);
    }

    /**
     * 회원 탈퇴를 위한 비밀번호 검증 (타이밍 공격 방지 포함)
     * @param localAuth 로컬 인증 정보
     * @param password  사용자가 입력한 비밀번호
     * @param userId    사용자 ID (로깅용)
     */
    private void verifyPasswordForDeletion(UserAuth localAuth, String password, Integer userId) {
        timingAttackProtection.startTiming();
        try {
            boolean passwordMatches = passwordEncoder.matches(password, localAuth.getPasswordHash());
            long delay = authProperties.getSecurity().getTimingAttackDelayMs();

            if (!passwordMatches) {
                timingAttackProtection.ensureMinimumResponseTime(delay);
                log.warn("[ACCOUNT_DELETE] 회원 탈퇴 실패 - 비밀번호 불일치: userId={}", userId);
                throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
            }

            timingAttackProtection.ensureMinimumResponseTime(delay);
        } finally {
            timingAttackProtection.clear();
        }
    }
}
