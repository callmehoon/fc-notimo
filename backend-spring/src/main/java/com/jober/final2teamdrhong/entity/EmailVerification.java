package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이메일 인증 코드 엔티티 (RDB 저장용)
 * Redis나 InMemory가 사용 불가능할 때 fallback으로 사용
 */
@Entity
@Table(name = "email_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;

    // Builder 패턴을 위한 생성자
    @Builder
    private EmailVerification(String email, String verificationCode,
                              LocalDateTime expiresAt, Integer attempts, Boolean isUsed) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt != null ? expiresAt : LocalDateTime.now().plusMinutes(5);
        this.createdAt = LocalDateTime.now();
        this.attempts = attempts != null ? attempts : 0;
        this.isUsed = isUsed != null ? isUsed : false;
    }

    // 비즈니스 메서드: 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 비즈니스 메서드: 시도 횟수 증가
    public EmailVerification incrementAttempts() {
        return EmailVerification.builder()
                .email(this.email)
                .verificationCode(this.verificationCode)
                .expiresAt(this.expiresAt)
                .attempts(this.attempts + 1)
                .isUsed(this.isUsed)
                .build();
    }

    // 비즈니스 메서드: 사용 처리
    public EmailVerification markAsUsed() {
        return EmailVerification.builder()
                .email(this.email)
                .verificationCode(this.verificationCode)
                .expiresAt(this.expiresAt)
                .attempts(this.attempts)
                .isUsed(true)
                .build();
    }

    // 비즈니스 메서드: 최대 시도 횟수 초과 여부
    public boolean isMaxAttemptsExceeded() {
        return attempts >= 5; // 최대 5회 시도
    }

    // 정적 팩토리 메서드
    public static EmailVerification create(String email, String verificationCode, int validMinutes) {
        return EmailVerification.builder()
                .email(email)
                .verificationCode(verificationCode)
                .expiresAt(LocalDateTime.now().plusMinutes(validMinutes))
                .attempts(0)
                .isUsed(false)
                .build();
    }
}
