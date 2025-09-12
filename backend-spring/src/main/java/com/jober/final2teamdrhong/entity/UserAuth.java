package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "users_auth",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_type_social_id",
                columnNames = {"auth_type","social_id"})
    },
    indexes = {
        @Index(name = "idx_user_auth_user_id", columnList = "users_id"),
        @Index(name = "idx_user_auth_type", columnList = "auth_type"),
        @Index(name = "idx_user_auth_last_used", columnList = "last_used_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("is_deleted = false")
public class UserAuth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Integer authId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    // 양방향 관계 설정을 위한 편의 메서드
    protected void setUser(User user) {
        this.user = user;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    private AuthType authType;

    @Column(name = "social_id")
    private String socialId;

    @Column(name = "password_hash")
    private String passwordHash;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = true;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Builder.Default
    @Column(name = "linked_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime linkedAt = LocalDateTime.now();

    @Column(name = "last_used_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUsedAt;

    public enum AuthType {
        LOCAL, GOOGLE, KAKAO, NAVER
    }

    // 로컬 인증 생성을 위한 정적 팩토리 메소드 추가
    public static UserAuth createLocalAuth(User user, String passwordHash) {
        return UserAuth.builder()
                .user(user)
                .authType(AuthType.LOCAL)
                .passwordHash(passwordHash)
                .isVerified(false) // <-- 조건부 로직을 여기에 명확하게 기술
                .build();
    }

    // 소셜 인증 생성을 위한 정적 팩토리 메소드 추가
    public static UserAuth createSocialAuth(User user, AuthType authType, String socialId) {
        if (authType == AuthType.LOCAL) {
            throw new IllegalArgumentException("소셜 인증 타입이 필요합니다.");
        }
        return UserAuth.builder()
                .user(user)
                .authType(authType)
                .socialId(socialId)
                .isVerified(true) // <-- 조건부 로직을 여기에 명확하게 기술
                .build();
    }

    // --- 비즈니스 메서드 ---
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void markAsVerified() {
        if (!this.isVerified) {
            this.isVerified = true;
        }
    }

    public void setAsPrimary() {
        this.isPrimary = true;
    }

    public void unsetAsPrimary() {
        this.isPrimary = false;
    }
}