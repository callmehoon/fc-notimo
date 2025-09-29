package com.jober.final2teamdrhong.dto.user;

import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 조회 응답 DTO (읽기 전용)
 */
@Schema(description = "사용자 프로필 정보 (읽기 전용)")
public record UserProfileResponse(
    @Schema(description = "사용자 ID", example = "1")
    Integer userId,

    @Schema(description = "사용자 이름", example = "홍길동")
    String userName,

    @Schema(description = "이메일 주소", example = "user@example.com")
    String userEmail,

    @Schema(description = "전화번호", example = "010-1234-5678")
    String userNumber,

    @Schema(description = "사용자 권한", example = "USER")
    String userRole,

    @Schema(description = "가입일", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "연결된 로그인 방법")
    AuthMethodsInfo authMethods
) {

    /**
     * User 엔티티로부터 응답 DTO 생성
     */
    public static UserProfileResponse from(User user) {
        // 연결된 인증 방법 정보 생성
        boolean hasLocal = user.getUserAuths().stream()
                .anyMatch(auth -> auth.getAuthType() == UserAuth.AuthType.LOCAL);

        java.util.List<String> socialMethods = user.getUserAuths().stream()
                .filter(auth -> auth.getAuthType() != UserAuth.AuthType.LOCAL)
                .map(auth -> auth.getAuthType().name())
                .toList();

        AuthMethodsInfo authMethodsInfo = new AuthMethodsInfo(hasLocal, socialMethods);

        return new UserProfileResponse(
            user.getUserId(),
            user.getUserName(),
            user.getUserEmail(),
            user.getUserNumber(),
            user.getUserRole().name(),
            user.getCreatedAt(),
            authMethodsInfo
        );
    }

    /**
     * 인증 방법 정보 내부 record 클래스
     */
    @Schema(description = "연결된 인증 방법 정보")
    public record AuthMethodsInfo(
        @Schema(description = "로컬 로그인 가능 여부")
        boolean hasLocalAuth,

        @Schema(description = "소셜 로그인 방법 목록")
        java.util.List<String> socialMethods
    ) {}
}