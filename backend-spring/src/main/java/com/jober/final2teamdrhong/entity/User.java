package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usersss")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usersss_id")
    private Integer userId;

    @Column(name = "usersss_name")
    private String userName;

    @Column(name = "usersss_number")
    private String userNumber;

    @Column(unique = true, nullable = false, name = "usersss_email")
    private String userEmail;

    // BaseTimeEntity 제거  시간필드 직접 추가
    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;


    @Column(name = "usersss_role")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public enum UserRole {
        USER,
        ADMIN
    }


    @OneToMany(mappedBy = "usersss", cascade = CascadeType.ALL, orphanRemoval = true) // ✅ Cascade 옵션 추가
    private List<UserAuth> userAuths = new ArrayList<>();

    // 생성자 (package-private)
    @Builder
    User(String userName, String userEmail, String userNumber, UserRole userRole) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userNumber = userNumber;
        this.userRole = ( userRole != null ) ? userRole : UserRole.USER;
    }

    // --- 관계 편의 메서드 ---
    public void addUserAuth(UserAuth userAuth) {
        this.userAuths.add(userAuth);
        userAuth.setUser(this); // UserAuth에도 User를 설정 (양방향)
    }

    public boolean isAdmin() {
        return this.userRole == UserRole.ADMIN;
    }

    public void updateInfo(String userName, String userEmail, String userNumber) {
        if (userName != null && !userName.trim().isEmpty()) {
            this.userName = userName;
        }

        if (userNumber != null && !userNumber.trim().isEmpty()) {
            this.userNumber = userNumber;
        }
    }
}
