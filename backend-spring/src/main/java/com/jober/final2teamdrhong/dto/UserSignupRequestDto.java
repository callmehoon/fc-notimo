package com.jober.final2teamdrhong.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder // Builder는 final 필드를 초기화 하는 생성자를 자동으로 만들어줌, 불변성을 위해 @Setter 사용안함
public class UserSignupRequestDto {

    //사용자 기본 정보
    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(min = 2 , max = 50 , message = "사용자 이름은 2자 이상 50자 이하여야 합니다.")
    private final String userName;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private final String email;

    @Pattern(regexp = "^[0-9]{8,20}$",message = "사용자 번호는 8-20자리 숫자여야 합니다.")
    private final String userNumber;

    // 로컬 회원/로그인 비밀번호
    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,20}$",
             message = "비밀번호는 6-20자의 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    private final String password;

    @NotBlank(message = "이메일 인증 코드는 필수입니다.")
    private final String verificationCode;



}
