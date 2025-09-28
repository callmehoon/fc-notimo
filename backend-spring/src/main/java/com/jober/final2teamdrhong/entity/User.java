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


    // 정적 팩토리 메서드
    public static User create(String userName, String userEmail, String userNumber) {
        return User.builder()
                .userName(userName)
                .userEmail(userEmail)
                .userNumber(userNumber)
                .userRole(UserRole.USER) // 명시적으로 USER 권한 설정
                .build();
    }



    /**
     * 회원 탈퇴 처리
     * Soft Delete 방식으로 처리하며, 개인정보를 익명화합니다.
     *
     * @param anonymizedEmail 익명화된 이메일 (예: deleted_user_12345@deleted.com)
     */
    public void deleteAccount(String anonymizedEmail) {
        // 1. 개인정보 익명화 처리
        this.userName = "탈퇴한 사용자";
        this.userEmail = anonymizedEmail;
        this.userNumber = "000-0000-0000";

        // 2. Soft Delete 처리 (BaseEntity의 메서드 활용)
        this.softDelete();

        // 3. 인증 정보도 모두 무효화
        this.userAuths.forEach(auth -> {
            auth.markAsDeleted();
        });
    }

    /**
     * 탈퇴한 회원인지 확인
     *
     * @return 탈퇴한 회원이면 true
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.getIsDeleted());
    }
}
