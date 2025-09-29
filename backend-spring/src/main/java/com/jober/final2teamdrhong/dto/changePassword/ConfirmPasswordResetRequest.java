package com.jober.final2teamdrhong.dto.changePassword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(
    description = "비밀번호 찾기(재설정) 요청 DTO - 이메일 인증 후 새 비밀번호 설정",
    example = """
    {
        "email": "user@example.com",
        "verificationCode": "123456",
        "newPassword": "NewPassword123!"
    }
    """
)
public record ConfirmPasswordResetRequest(
    @Schema(
        description = "비밀번호를 재설정할 계정의 이메일 주소",
        example = "user@example.com",
        format = "email"
    )
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @Schema(
        description = "이메일로 받은 6자리 인증 코드",
        example = "123456",
        pattern = "^[0-9]{6}$",
        minLength = 6,
        maxLength = 6
    )
    @NotBlank(message = "인증 코드를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
    String verificationCode,

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