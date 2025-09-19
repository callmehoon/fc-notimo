package com.jober.final2teamdrhong.dto.emailVerification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "이메일 인증 코드 발송 응답 DTO")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailVerificationResponse {

    @Schema(description = "성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 메시지", example = "인증 코드가 발송되었습니다.")
    private final String message;

    @Schema(description = "추가 데이터 (필요시)", example = "null")
    private final Object data;

    // 성공 응답
    public static EmailVerificationResponse success(String message) {
        return new EmailVerificationResponse(true, message, null);
    }

    public static EmailVerificationResponse success(String message, Object data) {
        return new EmailVerificationResponse(true, message, data);
    }

    // 실패 응답
    public static EmailVerificationResponse failure(String message) {
        return new EmailVerificationResponse(false, message, null);
    }
}