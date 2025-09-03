package com.jober.final2teamdrhong.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "user_auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuth extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Integer authId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type")
    private AuthType authType;

    public enum AuthType {
        LOCAL, GOOGLE, KAKAO, NAVER
    }

    @Column(name = "social_id")
    private String socialId;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "social_email")
    private String socialEmail;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "linked_at", columnDefinition = "DATETIME")
    private LocalDateTime linkedAt;

    @Column(name = "last_used_at", columnDefinition = "DATETIME")
    private LocalDateTime lastUsedAt;

    // 로컬 인증용 생성자 (package-private)
    UserAuth(User user, String passwordHash) {
        this.user = user;
        this.authType = AuthType.LOCAL;
        this.passwordHash = passwordHash;
        this.isPrimary = true;
        this.isVerified = false;
        this.linkedAt = LocalDateTime.now();
    }

    // 소셜 인증용 생성자 (package-private)
    UserAuth(User user, AuthType authType, String socialId, String socialEmail, String issuer) {
        this.user = user;
        this.authType = authType;
        this.socialId = socialId;
        this.socialEmail = socialEmail;
        this.issuer = issuer;
        this.isPrimary = true;
        this.isVerified = true; // 소셜 로그인은 이미 인증된 상태
        this.linkedAt = LocalDateTime.now();
    }

    // 정적 팩토리 메서드: 로컬 인증 생성
    public static UserAuth ofLocal(User user, String passwordHash) {
        return new UserAuth(user, passwordHash);
    }

    // 정적 팩토리 메서드: 소셜 인증 생성
    public static UserAuth ofSocial(User user, AuthType authType, String socialId,
                                    String socialEmail, String issuer) {
        return new UserAuth(user, authType, socialId, socialEmail, issuer);
    }

    // 범용 정적 팩토리 메서드 (피드백 반영)
    public static UserAuth of(User user, AuthType authType) {
        if (authType == AuthType.LOCAL) {
            throw new IllegalArgumentException("로컬 인증은 ofLocal 메서드를 사용하세요.");
        }
        return new UserAuth(user, authType, null, null, null);
    }

    // 비즈니스 메서드 (간단한 상태 변경만)
    public boolean isVerified() {
        return Boolean.TRUE.equals(this.isVerified);
    }

    public boolean isPrimary() {
        return Boolean.TRUE.equals(this.isPrimary);
    }

    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void verify() {
        this.isVerified = true;
    }

    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
    }
}


