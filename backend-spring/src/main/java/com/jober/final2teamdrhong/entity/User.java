package com.jober.final2teamdrhong.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_number")
    private String userNumber;

    @Column(unique = true, nullable = false, name = "user_email")
    private String userEmail;

    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public enum UserRole {
        USER,
        ADMIN
    }

    // 생성자 (package-private)
    User(String userName, String userEmail, String userNumber) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userNumber = userNumber;
        this.userRole = UserRole.USER;
    }

    // 관리자용 생성자 (package-private)
    User(String userName, String userEmail, String userNumber, UserRole userRole) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userNumber = userNumber;
        this.userRole = userRole;
    }

    // 정적 팩토리 메서드
    public static User of(String userName, String userEmail, String userNumber) {
        return new User(userName, userEmail, userNumber);
    }

    public static User ofAdmin(String userName, String userEmail, String userNumber) {
        return new User(userName, userEmail, userNumber, UserRole.ADMIN);
    }

    // 비즈니스 메서드 (상태 조회)
    public boolean isAdmin() {
        return this.userRole == UserRole.ADMIN;
    }

    public boolean isUser() {
        return this.userRole == UserRole.USER;
    }

    // 비즈니스 메서드 (상태 변경 - void 메서드)
    public void promoteToAdmin() {
        this.userRole = UserRole.ADMIN;
    }

    public void demoteToUser() {
        this.userRole = UserRole.USER;
    }

    public void updateInfo(String userName, String userEmail, String userNumber) {
        if (userName != null && !userName.trim().isEmpty()) {
            this.userName = userName;
        }
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            this.userEmail = userEmail;
        }
        if (userNumber != null && !userNumber.trim().isEmpty()) {
            this.userNumber = userNumber;
        }
    }

    // 엔티티 검증 메서드
    public void validateForPromotion() {
        if (this.isAdmin()) {
            throw new IllegalStateException("이미 관리자입니다.");
        }
    }
}
