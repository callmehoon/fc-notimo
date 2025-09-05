package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 리프레시 토큰 엔티티 (RDB 저장용)
 * Redis 장애 시 Fallback 또는 보안 감사용으로 사용됩니다.
 */
@Entity
@Table(name = "refresh_token", indexes = {
    @Index(name = "idx_token_hash", columnList = "token_hash"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash; // 실제 토큰이 아닌, 해시된 값을 저장하여 보안 강화

    @Column(name = "expires_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false; // 토큰이 무효화되었는지 여부

    // 상수 정의
    private static final int DEFAULT_EXPIRY_DAYS = 7; // 7일
    
    @Builder
    private RefreshToken(User user, String tokenHash, LocalDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt != null ? expiresAt : LocalDateTime.now().plusDays(DEFAULT_EXPIRY_DAYS);
        this.createdAt = LocalDateTime.now();
        // isRevoked는 필드 초기화로 처리
    }

    // 정적 팩토리 메서드
    public static RefreshToken create(User user, String tokenHash, int expiryDays) {
        return RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusDays(expiryDays))
                .build();
    }
    
    public static RefreshToken create(User user, String tokenHash) {
        return create(user, tokenHash, DEFAULT_EXPIRY_DAYS);
    }
    
    // --- 비즈니스 메서드 ---
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void revoke() {
        this.isRevoked = true;
    }
    
    public boolean isValid() {
        return !isExpired() && !isRevoked;
    }
    
    // 비즈니스 메서드: 토큰 갱신 (기존 토큰 무효화 후 새 토큰 생성시 사용)
    public RefreshToken renewToken(String newTokenHash, int expiryDays) {
        this.revoke(); // 기존 토큰 무효화
        return RefreshToken.create(this.user, newTokenHash, expiryDays);
    }
}
