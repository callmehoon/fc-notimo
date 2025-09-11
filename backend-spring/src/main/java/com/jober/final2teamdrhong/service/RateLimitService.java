package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.config.RateLimitConfig;
import com.jober.final2teamdrhong.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.local.LocalBucketBuilder;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
@Slf4j
public class RateLimitService {
    
    private final RateLimitConfig rateLimitConfig;
    private final LettuceBasedProxyManager<byte[]> proxyManager;
    
    // 인메모리 버킷 저장소 (Redis 비활성화 시 폴백용)
    private final Map<String, Bucket> inMemoryBuckets = new ConcurrentHashMap<>();
    
    @Autowired
    public RateLimitService(RateLimitConfig rateLimitConfig, 
                           @Autowired(required = false) StringRedisTemplate redisTemplate,
                           @Autowired(required = false) LettuceBasedProxyManager<byte[]> proxyManager) {
        this.rateLimitConfig = rateLimitConfig;
        this.proxyManager = proxyManager;
    }
    
    public boolean isEmailSendAllowed(String ipAddress) {
        String key = "email_send:" + ipAddress;
        Bucket bucket = getBucket(key, () -> createEmailSendBucketConfig());
        return bucket.tryConsume(1);
    }
    
    public boolean isEmailVerifyAllowed(String email) {
        String key = "email_verify:" + email;
        Bucket bucket = getBucket(key, () -> createEmailVerifyBucketConfig());
        return bucket.tryConsume(1);
    }
    
    public boolean isSignupAllowed(String ipAddress) {
        String key = "signup:" + ipAddress;
        Bucket bucket = getBucket(key, () -> createSignupBucketConfig());
        return bucket.tryConsume(1);
    }
    
    public long getEmailSendWaitTime(String ipAddress) {
        String key = "email_send:" + ipAddress;
        Bucket bucket = getBucket(key, () -> createEmailSendBucketConfig());
        return bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000;
    }
    
    public long getEmailVerifyWaitTime(String email) {
        String key = "email_verify:" + email;
        Bucket bucket = getBucket(key, () -> createEmailVerifyBucketConfig());
        return bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000;
    }
    
    public long getSignupWaitTime(String ipAddress) {
        String key = "signup:" + ipAddress;
        Bucket bucket = getBucket(key, () -> createSignupBucketConfig());
        return bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000;
    }
    
    private Bucket getBucket(String key, Supplier<BucketConfiguration> configSupplier) {
        if (proxyManager != null) {
            // Redis 사용 가능한 경우
            return proxyManager.builder()
                    .build(key.getBytes(), configSupplier);
        } else {
            // Redis 비활성화 시 인메모리 폴백 사용
            return inMemoryBuckets.computeIfAbsent(key, k -> 
                Bucket.builder()
                    .addLimit(getBandwidthFromConfig(configSupplier.get()))
                    .build()
            );
        }
    }
    
    private Bandwidth getBandwidthFromConfig(BucketConfiguration config) {
        // BucketConfiguration에서 첫 번째 Bandwidth 추출
        Bandwidth[] bandwidths = config.getBandwidths();
        if (bandwidths.length == 0) {
            throw new IllegalArgumentException("BucketConfiguration must have at least one bandwidth");
        }
        return bandwidths[0];
    }
    
    private BucketConfiguration createEmailSendBucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(
                        rateLimitConfig.getEmailSend().getRequestsPerWindow(),
                        Duration.ofMinutes(rateLimitConfig.getEmailSend().getWindowDurationMinutes())
                ))
                .build();
    }
    
    private BucketConfiguration createEmailVerifyBucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(
                        rateLimitConfig.getEmailVerify().getRequestsPerWindow(),
                        Duration.ofMinutes(rateLimitConfig.getEmailVerify().getWindowDurationMinutes())
                ))
                .build();
    }
    
    private BucketConfiguration createSignupBucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(
                        rateLimitConfig.getSignup().getRequestsPerWindow(),
                        Duration.ofMinutes(rateLimitConfig.getSignup().getWindowDurationMinutes())
                ))
                .build();
    }
    
    // === 편의 메서드들 (중복 로직 제거) ===
    
    /**
     * 이메일 발송 Rate Limiting 체크
     */
    public void checkEmailSendRateLimit(String clientIp, String email) {
        if (!isEmailSendAllowed(clientIp)) {
            long waitTime = getEmailSendWaitTime(clientIp);
            log.warn("이메일 발송 속도 제한 초과: ip={}, email={}, waitTime={}초", 
                    clientIp, email, waitTime);
            throw new RateLimitExceededException(
                "이메일 발송 속도 제한을 초과했습니다. " + waitTime + "초 후 다시 시도해주세요.", 
                waitTime
            );
        }
    }

    /**
     * 회원가입 Rate Limiting 체크
     */
    public void checkSignupRateLimit(String clientIp, String email) {
        if (!isSignupAllowed(clientIp)) {
            long waitTime = getSignupWaitTime(clientIp);
            log.warn("회원가입 속도 제한 초과: ip={}, email={}, waitTime={}초", 
                    clientIp, email, waitTime);
            throw new RateLimitExceededException(
                "회원가입 속도 제한을 초과했습니다. " + waitTime + "초 후 다시 시도해주세요.", 
                waitTime
            );
        }
    }

    /**
     * 이메일 인증 코드 검증 Rate Limiting 체크
     */
    public void checkEmailVerifyRateLimit(String email) {
        if (!isEmailVerifyAllowed(email)) {
            long waitTime = getEmailVerifyWaitTime(email);
            log.warn("인증 코드 검증 속도 제한 초과: email={}, waitTime={}초", email, waitTime);
            throw new RateLimitExceededException(
                "인증 코드 검증 시도 횟수를 초과했습니다. " + waitTime + "초 후 다시 시도해주세요.", 
                waitTime
            );
        }
    }
}