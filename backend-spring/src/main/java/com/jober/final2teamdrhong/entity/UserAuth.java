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

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Builder.Default
    @Column(name = "linked_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime linkedAt = LocalDateTime.now();

    @Column(name = "last_used_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUsedAt;

    public enum AuthType {
        LOCAL("Local", null, null, null, false),
        GOOGLE("Google", "/api/auth/social/login/google",
               "https://developers.google.com/identity/images/g-logo.png",
               "구글 계정으로 간편 로그인", true),
        KAKAO("Kakao", "/api/auth/social/login/kakao",
              "https://developers.kakao.com/assets/img/about/logos/kakaolink/kakaolink_btn_medium.png",
              "카카오 계정으로 간편 로그인", false),  // 향후 지원 예정
        NAVER("Naver", "/api/auth/social/login/naver",
              "https://developers.naver.com/inc/devcenter/uploads/2017/1/2017_naver_logo_03.png",
              "네이버 계정으로 간편 로그인", false);  // 향후 지원 예정

        private final String displayName;
        private final String loginUrl;
        private final String iconUrl;
        private final String description;
        private final boolean enabled;

        AuthType(String displayName, String loginUrl, String iconUrl, String description, boolean enabled) {
            this.displayName = displayName;
            this.loginUrl = loginUrl;
            this.iconUrl = iconUrl;
            this.description = description;
            this.enabled = enabled;
        }

        public String getDisplayName() { return displayName; }
        public String getLoginUrl() { return loginUrl; }
        public String getIconUrl() { return iconUrl; }
        public String getDescription() { return description; }
        public boolean isEnabled() { return enabled; }
        public boolean isSocial() { return this != LOCAL; }

        /**
         * 현재 지원하는 소셜 로그인 제공자 목록 반환
         */
        public static AuthType[] getSupportedSocialProviders() {
            return java.util.Arrays.stream(values())
                    .filter(type -> type.isSocial() && type.isEnabled())
                    .toArray(AuthType[]::new);
        }

        /**
         * 제공자 이름으로 AuthType 찾기 (대소문자 무시)
         */
        public static AuthType fromProvider(String provider) {
            if (provider == null) return null;
            try {
                return AuthType.valueOf(provider.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
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