package com.jober.final2teamdrhong.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 회원 탈퇴 요청 DTO
 * 보안을 위해 현재 비밀번호와 확인 문구를 요구합니다.
 */
@Schema(
    description = "회원 탈퇴 요청 DTO - 현재 비밀번호와 확인 문구 필요",
    example = """
    {
        "password": "CurrentPassword123!",
        "confirmText": "회원탈퇴"
    }
    """
)
public record DeleteUserRequest(
    @Schema(
        description = "현재 비밀번호 - 본인 확인용",
        example = "CurrentPassword123!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password,

    @Schema(
        description = "탈퇴 확인 문구 - '회원탈퇴' 입력 필요",
        example = "회원탈퇴",
        pattern = "^회원탈퇴$",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "확인 문구를 입력해주세요.")
    @Pattern(
        regexp = "^회원탈퇴$",
        message = "탈퇴를 원하시면 '회원탈퇴'를 정확히 입력해주세요."
    )
    String confirmText
) {}