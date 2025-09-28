package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.config.JwtConfig;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.exception.AuthenticationException;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.util.LogMaskingUtil;
import com.jober.final2teamdrhong.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.util.Set;

/**
 * RefreshToken 관리 서비스
 * 
 * 주요 기능:
 * - Refresh Token 생성, 검증, 갱신
 * - 토큰 회전 (Token Rotation) 보안 정책
 * - 다중 세션 관리 및 제한
 * - 토큰 정리 및 통계
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    private final RateLimitService rateLimitService;
    private final RedisTemplate<String,String> redisTemplate;
    private final AuthProperties authProperties;
    private final BlacklistService blacklistService;

    // Redis 키 패턴 및 설정 상수는 AuthProperties에서 가져옴
    /**
     * 사용자용 Refresh Token 생성 및 저장 (Redis)
     * 기존 토큰이 있으면 새로 교체 (단일 세션 정책)
     */
    public String createRefreshToken(User user, String clientIp) {
        log.info("Refresh Token 생성 시작: userId={}, ip={}", 
                LogMaskingUtil.maskUserId(user.getUserId().longValue()), 
                LogMaskingUtil.maskIpAddress(clientIp));

        // 1. 기존 토큰 무효화 (단일 세션 정책)
        revokeAllUserTokens(user.getUserId().longValue());

        // 2. 새로운 Refresh Token 생성
        String refreshToken = jwtConfig.generateRefreshToken(user.getUserEmail(), user.getUserId());
        String tokenHash = jwtConfig.generateTokenHash(refreshToken);

        // 3. Redis에 저장
        saveRefreshTokenToRedis(tokenHash,user.getUserId().longValue(),authProperties.getToken().getRefreshTokenValiditySeconds());

        log.info("Refresh Token 생성 완료: userId={}",
                LogMaskingUtil.maskUserId(user.getUserId().longValue()));

        return refreshToken;
    }

    /**
     * Refresh Token으로 새로운 Access Token + Refresh Token 발급
     * 토큰 회전(Token Rotation) 보안 정책 적용
     */
    public TokenPair refreshTokens(String refreshToken, String clientIp) {
        log.info("토큰 갱신 요청: ip={}", LogMaskingUtil.maskIpAddress(clientIp));

        // 1. 토큰 유효성 검증
        validateRefreshTokenRequest(refreshToken, clientIp);

        // 2. 사용자 정보 추출 및 검증
        RefreshTokenContext context = extractAndValidateUserFromToken(refreshToken);

        // 3. 새로운 토큰 쌍 생성 및 저장
        TokenPair newTokens = generateAndStoreNewTokens(context, refreshToken);

        log.info("토큰 갱신 완료: userId={}", LogMaskingUtil.maskUserId(context.userId().longValue()));

        return newTokens;
    }

    /**
     * Refresh Token 요청 유효성 검증
     * Rate Limiting과 JWT 기본 검증을 수행합니다.
     *
     * @param refreshToken 검증할 Refresh Token
     * @param clientIp 클라이언트 IP
     */
    private void validateRefreshTokenRequest(String refreshToken, String clientIp) {
        // Rate Limiting 체크
        rateLimitService.checkLoginRateLimit(clientIp);

        // JWT 기본 검증
        if (!jwtConfig.validateToken(refreshToken) || !jwtConfig.isRefreshToken(refreshToken)) {
            log.warn("유효하지 않은 Refresh Token: ip={}", LogMaskingUtil.maskIpAddress(clientIp));
            throw new AuthenticationException(authProperties.getMessages().getInvalidRefreshToken());
        }

        // Redis에서 토큰 존재 여부 확인
        String tokenHash = jwtConfig.generateTokenHash(refreshToken);
        if (!isValidRefreshTokenInRedis(tokenHash)) {
            log.warn("Redis에서 유효하지 않은 Refresh Token");
            throw new AuthenticationException(authProperties.getMessages().getExpiredRefreshToken());
        }
    }

    /**
     * 토큰에서 사용자 정보 추출 및 검증
     *
     * @param refreshToken Refresh Token
     * @return 검증된 사용자 컨텍스트 정보
     */
    private RefreshTokenContext extractAndValidateUserFromToken(String refreshToken) {
        // 토큰에서 사용자 정보 추출
        String email = jwtConfig.getEmailFromToken(refreshToken);
        Integer userId = getUserIdFromTokenAsInteger(refreshToken);

        if (email == null || userId == null) {
            log.warn("토큰에서 사용자 정보 추출 실패");
            throw new AuthenticationException(authProperties.getMessages().getInvalidTokenInfo());
        }

        // DB에서 사용자 조회
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new AuthenticationException(authProperties.getMessages().getUserNotFound()));

        // 사용자 인증 상태 확인
        validateUserVerificationStatus(user, userId);

        return new RefreshTokenContext(email, userId, user);
    }

    /**
     * 사용자 인증 상태 검증
     *
     * @param user 사용자 엔티티
     * @param userId 사용자 ID
     */
    private void validateUserVerificationStatus(User user, Integer userId) {
        boolean isVerified = user.getUserAuths().stream()
                .anyMatch(auth -> Boolean.TRUE.equals(auth.getIsVerified()));

        if (!isVerified) {
            log.warn("미인증 사용자의 토큰 갱신 시도: userId={}", userId);
            throw new AuthenticationException("이메일 인증이 완료되지 않은 사용자입니다.");
        }
    }

    /**
     * 새로운 토큰 쌍 생성 및 저장
     *
     * @param context 사용자 컨텍스트 정보
     * @param oldRefreshToken 기존 Refresh Token (무효화용)
     * @return 새로운 토큰 쌍
     */
    private TokenPair generateAndStoreNewTokens(RefreshTokenContext context, String oldRefreshToken) {
        // 새로운 토큰 쌍 생성
        String newAccessToken = jwtConfig.generateAccessToken(context.email(), context.userId());
        String newRefreshToken = jwtConfig.generateRefreshToken(context.email(), context.userId());
        String newTokenHash = jwtConfig.generateTokenHash(newRefreshToken);

        // 기존 토큰 무효화 및 새 토큰 저장 (Token Rotation)
        revokeRefreshToken(oldRefreshToken);
        saveRefreshTokenToRedis(newTokenHash, context.userId().longValue(),
                authProperties.getToken().getRefreshTokenValiditySeconds());

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    /**
     * Refresh Token 갱신 시 필요한 사용자 컨텍스트 정보
     *
     * @param email 사용자 이메일
     * @param userId 사용자 ID
     * @param user 사용자 엔티티
     */
    private record RefreshTokenContext(String email, Integer userId, User user) {}

    /**
     * Redis에 Refresh Token 저장
     *
     * @param tokenHash 토큰 해시값
     * @param userId 사용자 ID
     * @param ttlSeconds 토큰 만료 시간 (초 단위)
     */
    private void saveRefreshTokenToRedis(String tokenHash, Long userId, long ttlSeconds) {
        try{
            String tokenKey = authProperties.getRedis().getRefreshTokenKeyPrefix() + tokenHash;
            String userTokensSetKey = authProperties.getRedis().getUserTokensKeyPrefix() + userId; // 사용자별 토큰 목록을 위한 키

            // 토큰 해시 -> 사용자 ID 매핑 저장
            redisTemplate.opsForValue().set(tokenKey, userId.toString(), Duration.ofSeconds(ttlSeconds));

            // 사용자 별 토큰 목록에 추가 (정리용)
            redisTemplate.opsForSet().add(userTokensSetKey, tokenHash);
            redisTemplate.expire(userTokensSetKey, Duration.ofSeconds(ttlSeconds));

            log.info("Refresh Token 저장 완료: userId={}, TTL={}초", LogMaskingUtil.maskUserId(userId), ttlSeconds);
        }catch (Exception e){
            log.error("Redis Refresh Token 저장 실패 : {}", e.getMessage());
            throw new RuntimeException("Failed to save Refresh Token",e);
        }
    }


    /**
     * Redis에서 Refresh Token 검증
     */
    private boolean isValidRefreshTokenInRedis(String tokenHash) {
        try {
            String tokenKey = authProperties.getRedis().getRefreshTokenKeyPrefix() + tokenHash;
            Boolean exists = redisTemplate.hasKey(tokenKey);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Redis Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 특정 Refresh Token 무효화
     */
    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            return;
        }

        String tokenHash = jwtConfig.generateTokenHash(refreshToken);

        try {
            String tokenKey = authProperties.getRedis().getRefreshTokenKeyPrefix() + tokenHash;
            String userIdStr = redisTemplate.opsForValue().get(tokenKey);

            if (userIdStr != null) {
                Long userId = Long.valueOf(userIdStr);
                String userTokensSetKey = authProperties.getRedis().getUserTokensKeyPrefix() + userId;

                // 토큰 삭제
                redisTemplate.delete(tokenKey);
                // 사용자별 토큰 목록에서도 삭제
                redisTemplate.opsForSet().remove(userTokensSetKey, tokenHash);

                log.info("Refresh Token 무효화 완료: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("Redis Refresh Token 무효화 실패: {}", e.getMessage());
        }
    }

    /**
     * Access Token을 사용자별 토큰 목록에 저장
     *
     * @param accessToken Access Token
     * @param userId 사용자 ID
     */
    public void saveAccessToken(String accessToken, Integer userId) {
        if (accessToken == null || userId == null) {
            log.warn("Access Token 저장 실패 - 필수 파라미터 누락: token={}, userId={}",
                accessToken != null ? "존재" : "null", userId);
            return;
        }

        try {
            String jti = jwtConfig.getJtiFromToken(accessToken);
            if (jti == null) {
                log.warn("Access Token에서 JTI 추출 실패: userId={}", userId);
                return;
            }

            String userTokensSetKey = authProperties.getRedis().getUserTokensKeyPrefix() + userId.longValue();

            // Access Token JTI를 prefix와 함께 저장 (Refresh Token과 구분)
            String accessTokenId = authProperties.getToken().getAccessTokenPrefix() + jti;
            redisTemplate.opsForSet().add(userTokensSetKey, accessTokenId);

            // TTL 설정 (Access Token 만료시간에 맞춤)
            long accessTokenValiditySeconds = jwtConfig.getAccessTokenValiditySeconds();
            redisTemplate.expire(userTokensSetKey, Duration.ofSeconds(accessTokenValiditySeconds));

            log.debug("Access Token 저장 완료: userId={}, jti={}", userId, jti);

        } catch (Exception e) {
            log.error("Access Token 저장 실패: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 사용자의 모든 Refresh Token 무효화 (전체 로그아웃)
     */
    public void revokeAllUserTokens(Long userId) {
        try {
            String userTokensSetKey = authProperties.getRedis().getUserTokensKeyPrefix() + userId;
            Set<String> tokenIdentifiers = redisTemplate.opsForSet().members(userTokensSetKey);

            if (tokenIdentifiers != null && !tokenIdentifiers.isEmpty()) {
                // 토큰 타입별로 처리
                for (String tokenId : tokenIdentifiers) {
                    processTokenRevocation(tokenId);
                }
                redisTemplate.delete(userTokensSetKey); // 사용자 토큰 세트 자체 삭제
            }

            log.info("사용자 {}의 모든 토큰이 무효화되었습니다: {} 개", userId,
                tokenIdentifiers != null ? tokenIdentifiers.size() : 0);
        } catch (Exception e) {
            log.error("Redis 사용자 토큰 무효화 실패: {}", e.getMessage());
        }
    }

    /**
     * 사용자의 모든 토큰을 블랙리스트에 추가
     * 비밀번호 변경 시 사용
     *
     * @param userId 사용자 ID
     */
    public void addAllUserTokensToBlacklist(Integer userId) {
        if (userId == null) {
            log.warn("블랙리스트 추가 실패 - userId가 null");
            return;
        }

        try {
            String userTokensSetKey = authProperties.getRedis().getUserTokensKeyPrefix() + userId.longValue();
            Set<String> tokenIdentifiers = redisTemplate.opsForSet().members(userTokensSetKey);

            if (tokenIdentifiers == null || tokenIdentifiers.isEmpty()) {
                log.info("사용자 {}의 활성 토큰이 없음", userId);
                return;
            }

            int blacklistedCount = 0;

            // 각 토큰을 블랙리스트에 추가
            for (String tokenId : tokenIdentifiers) {
                if (addTokenToBlacklistByType(tokenId)) {
                    blacklistedCount++;
                }
            }

            // 사용자 토큰 목록 정리
            redisTemplate.delete(userTokensSetKey);

            log.info("사용자 {}의 모든 토큰 블랙리스트 추가 완료: {}/{} 개",
                userId, blacklistedCount, tokenIdentifiers.size());

        } catch (Exception e) {
            log.error("사용자 {} 토큰 블랙리스트 추가 실패: {}", userId, e.getMessage());
        }
    }

    /**
     * 토큰 타입별 무효화 처리 (단일 책임 원칙)
     *
     * @param tokenId 토큰 식별자 (prefix 포함)
     */
    private void processTokenRevocation(String tokenId) {
        try {
            String accessPrefix = authProperties.getToken().getAccessTokenPrefix();
            String refreshPrefix = authProperties.getToken().getRefreshTokenPrefix();

            if (tokenId.startsWith(accessPrefix)) {
                // Access Token은 JTI만 저장되어 있으므로 별도 처리 불필요
                log.debug("Access Token 무효화: {}", tokenId);
            } else if (tokenId.startsWith(refreshPrefix)) {
                // Refresh Token JTI 처리 (향후 확장 시 사용)
                log.debug("Refresh Token JTI 무효화: {}", tokenId);
            } else {
                // 기존 방식 (Refresh Token 해시)
                String tokenKey = authProperties.getRedis().getRefreshTokenKeyPrefix() + tokenId;
                redisTemplate.delete(tokenKey);
                log.debug("Refresh Token 해시 무효화: {}", tokenId);
            }
        } catch (Exception e) {
            log.error("토큰 무효화 처리 실패: tokenId={}, error={}", tokenId, e.getMessage());
        }
    }

    /**
     * 토큰 타입별 블랙리스트 추가 (단일 책임 원칙)
     *
     * @param tokenId 토큰 식별자 (prefix 포함)
     * @return 성공 여부
     */
    private boolean addTokenToBlacklistByType(String tokenId) {
        try {
            String accessPrefix = authProperties.getToken().getAccessTokenPrefix();
            String refreshPrefix = authProperties.getToken().getRefreshTokenPrefix();

            if (tokenId.startsWith(accessPrefix)) {
                String jti = tokenId.substring(accessPrefix.length()); // prefix 제거
                long accessTokenTtl = jwtConfig.getAccessTokenValiditySeconds();
                addJtiToBlacklist(jti, accessTokenTtl);
                return true;
            } else if (tokenId.startsWith(refreshPrefix)) {
                String jti = tokenId.substring(refreshPrefix.length()); // prefix 제거
                long refreshTokenTtl = authProperties.getToken().getRefreshTokenValiditySeconds();
                addJtiToBlacklist(jti, refreshTokenTtl);
                return true;
            } else {
                // 기존 방식은 이미 무효화되어 있으므로 별도 처리 불필요
                log.debug("기존 방식 토큰은 블랙리스트 추가 생략: {}", tokenId);
                return true;
            }
        } catch (Exception e) {
            log.error("토큰 블랙리스트 추가 실패: tokenId={}, error={}", tokenId, e.getMessage());
            return false;
        }
    }

    /**
     * JTI를 블랙리스트에 추가 (DRY 원칙)
     * BlacklistService를 활용하여 일관된 처리
     *
     * @param jti JWT ID
     * @param ttlSeconds TTL (초)
     */
    private void addJtiToBlacklist(String jti, long ttlSeconds) {
        try {
            // BlacklistService에 위임하여 일관된 처리
            blacklistService.addJtiToBlacklistExternal(jti, ttlSeconds);
            log.debug("JTI 블랙리스트 추가 완료: jti={}, ttl={}초", jti, ttlSeconds);
        } catch (Exception e) {
            log.error("JTI 블랙리스트 추가 실패: jti={}, error={}", jti, e.getMessage());
        }
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean isValidRefreshToken(String refreshToken) {
        if (refreshToken == null || !jwtConfig.validateToken(refreshToken) || !jwtConfig.isRefreshToken(refreshToken)) {
            return false;
        }

        String tokenHash = jwtConfig.generateTokenHash(refreshToken);
        return isValidRefreshTokenInRedis(tokenHash);
    }

    /**
     * 토큰 쌍 record DTO
     */
    public record TokenPair(String accessToken, String refreshToken) {}
    
    /**
     * JWT 토큰에서 사용자 ID를 Integer로 추출하는 helper 메서드
     */
    private Integer getUserIdFromTokenAsInteger(String token) {
        Long userId = jwtConfig.getUserIdFromToken(token);
        return userId != null ? userId.intValue() : null;
    }
}