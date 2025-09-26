package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "users_email"),
    @Index(name = "idx_user_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("is_deleted = false")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Integer userId;

    @Column(name = "users_name")
    private String userName;

    @Column(name = "users_number")
    private String userNumber;

    @Column(unique = true, nullable = false, name = "users_email")
    private String userEmail;

    @Builder.Default
    @Column(name = "users_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.USER;

    public enum UserRole {
        USER,
        ADMIN
    }

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAuth> userAuths = new ArrayList<>();

    // --- 관계 편의 메서드 ---
    public void addUserAuth(UserAuth userAuth) {
        this.userAuths.add(userAuth);
        userAuth.setUser(this); // UserAuth에도 User를 설정 (양방향)
    }

    public boolean isAdmin() {
        return this.userRole == UserRole.ADMIN;
    }

    // 정적 팩토리 메서드
    public static User create(String userName, String userEmail, String userNumber) {
        return User.builder()
                .userName(userName)
                .userEmail(userEmail)
                .userNumber(userNumber)
                .userRole(UserRole.USER) // 명시적으로 USER 권한 설정
                .build();
    }

    // 비즈니스 메서드: 정보 업데이트
    public void updateInfo(String userName, String userNumber) {
        if (userName != null && !userName.isEmpty()) {
            this.userName = userName;
        }

        if (userNumber != null && !userNumber.isEmpty()) {
            this.userNumber = userNumber;
        }
    }
}
