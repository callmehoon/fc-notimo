package com.jober.final2teamdrhong.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 소셜 로그인 후 회원가입 완료를 위한 요청 DTO
 * OAuth2 정보와 사용자가 입력한 핸드폰 번호를 결합하여 회원가입을 완료합니다.
 */
@Schema(
    description = "소셜 로그인 회원가입 완료 요청 DTO",
    example = """
    {
        "provider": "google",
        "socialId": "123456789012345678901",
        "email": "user@example.com",
        "name": "홍길동",
        "phoneNumber": "010-1234-5678",
        "agreedToTerms": true,
        "agreedToPrivacyPolicy": true,
        "agreedToMarketing": false
    }
    """
)
public record SocialSignupRequest(
    @Schema(description = "OAuth2 제공자", example = "google")
    @NotBlank(message = "OAuth2 제공자는 필수입니다.")
    String provider,

    @Schema(description = "소셜 로그인 제공자의 고유 ID", example = "123456789012345678901")
    @NotBlank(message = "소셜 ID는 필수입니다.")
    String socialId,

    @Schema(description = "사용자 이메일 주소", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    String email,

    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "사용자 이름은 필수입니다.")
    String name,

    @Schema(description = "핸드폰 번호 (010 번호만 가능)", example = "010-1234-5678")
    @NotBlank(message = "핸드폰 번호는 필수입니다.")
    @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$",
             message = "핸드폰 번호는 010-XXXX-XXXX 형식이어야 합니다.")
    String phoneNumber,

    @Schema(description = "약관 동의 여부", example = "true")
    @NotNull(message = "약관 동의는 필수입니다.")
    Boolean agreedToTerms,

    @Schema(description = "개인정보 처리방침 동의 여부", example = "true")
    @NotNull(message = "개인정보 처리방침 동의는 필수입니다.")
    Boolean agreedToPrivacyPolicy,

    @Schema(description = "마케팅 정보 수신 동의 여부 (선택사항)", example = "false")
    Boolean agreedToMarketing
) {
    /**
     * 기본값 설정된 생성자
     */
    public SocialSignupRequest {
        if (agreedToMarketing == null) {
            agreedToMarketing = false;
        }
    }

    /**
     * 핸드폰 번호에서 하이픈을 제거하고 정규화
     *
     * @return 정규화된 핸드폰 번호
     */
    public String getNormalizedPhoneNumber() {
        return phoneNumber != null ? phoneNumber.replaceAll("[.-]", "") : null;
    }

    /**
     * 필수 약관 동의 여부 확인
     *
     * @return 필수 약관 동의 완료 여부
     */
    public boolean hasRequiredAgreements() {
        return Boolean.TRUE.equals(agreedToTerms) &&
               Boolean.TRUE.equals(agreedToPrivacyPolicy);
    }
}