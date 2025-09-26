package com.jober.final2teamdrhong.dto.userLogin;

import com.jober.final2teamdrhong.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "로컬 로그인 응답 DTO")
public record UserLoginResponse(
    @Schema(description = "성공 여부", example = "true")
    boolean success,

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token,

    @Schema(description = "JWT 리프레시 토큰 (선택적)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken,

    @Schema(description = "토큰 타입", example = "Bearer")
    String tokenType,

    @Schema(description = "사용자 이름", example = "홍길동")
    String userName,

    @Schema(description = "사용자 권한", example = "USER")
    String userRole,

    @Schema(description = "로그인 시간", example = "2023-12-01T10:30:00")
    LocalDateTime loginTime,

    @Schema(description = "응답 메시지", example = "로그인이 성공적으로 완료되었습니다.")
    String message
) {
    // 정적 팩토리 메서드 : Refresh Token을 포함한 완전한 로그인 응답 생성
    public static UserLoginResponse withRefreshToken(User user, String accessToken, String refreshToken) {
        return new UserLoginResponse(
                true,
                accessToken,
                refreshToken,
                "Bearer",
                user.getUserName(),
                user.getUserRole().name(),
                LocalDateTime.now(),
                "로그인이 성공적으로 완료되었습니다."
        );
    }

    // 정적 팩토리 메서드 : 에러 응답 생성 (로그인 실패 시)
    public static UserLoginResponse error(String errorMessage) {
        return new UserLoginResponse(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                errorMessage
        );
    }
}
