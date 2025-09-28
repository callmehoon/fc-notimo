package com.jober.final2teamdrhong.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 소셜 로그인 사용자가 로컬 인증을 추가하는 요청 DTO
 * 마이페이지에서 계정 통합 시 사용
 */
@Schema(
    description = "계정 통합 요청 DTO - 소셜 로그인 사용자가 비밀번호를 설정하여 로컬 로그인 추가",
    example = """
    {
        "email": "user@example.com",
        "verificationCode": "123456",
        "password": "NewPassword123!"
    }
    """
)
public record AddLocalAuthRequest(
    @Schema(
        description = "이메일 주소 - 현재 계정의 이메일과 일치해야 함",
        example = "user@example.com",
        format = "email",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @Schema(
        description = "이메일로 받은 6자리 인증 코드",
        example = "123456",
        pattern = "^[0-9]{6}$",
        minLength = 6,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "인증 코드를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
    String verificationCode,

    @Schema(
        description = "설정할 비밀번호 - 보안 요구사항을 만족해야 함",
        example = "NewPassword123!",
        minLength = 6,
        maxLength = 20,
        pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,20}$",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하여야 합니다.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,20}$",
        message = "비밀번호는 6-20자의 대소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    String password
) {}