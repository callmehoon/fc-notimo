package com.jober.final2teamdrhong.dto.changePassword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(
    description = "마이페이지 비밀번호 변경 요청 DTO - 로그인한 사용자가 현재 비밀번호 확인 후 변경",
    example = """
    {
        "currentPassword": "OldPassword123!",
        "newPassword": "NewPassword123!"
    }
    """
)
public record PasswordResetRequest(
    @Schema(
        description = "현재 비밀번호",
        example = "OldPassword123!"
    )
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    String currentPassword,

    @Schema(
        description = "새로운 비밀번호 - 보안 요구사항을 만족해야 함",
        example = "NewPassword123!",
        minLength = 6,
        maxLength = 20,
        pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,20}$"
    )
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,20}$",
             message = "비밀번호는 6-20자의 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    String newPassword
) {}
