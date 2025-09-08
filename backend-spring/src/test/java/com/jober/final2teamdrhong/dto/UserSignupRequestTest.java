package com.jober.final2teamdrhong.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserSignupRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("성공: 유효한 모든 필드가 있으면 검증 통과")
    void validation_success_allValidFields() {
        // given
        UserSignupRequest dto = UserSignupRequest.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequest>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("실패: 사용자 이름이 빈 값이면 검증 실패")
    void validation_fail_emptyUserName() {
        // given
        UserSignupRequest dto = UserSignupRequest.builder()
                .userName("")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequest>> violations = validator.validate(dto);

        // then: @NotBlank와 @Size 검증이 모두 실패하므로 2개의 에러가 발생
        assertThat(violations).hasSize(2);
        assertThat(violations.stream().map(ConstraintViolation::getMessage))
                .containsAnyOf("사용자 이름은 필수입니다.", "사용자 이름은 2자 이상 50자 이하여야 합니다.");
    }

    @Test
    @DisplayName("실패: 사용자 이름이 너무 짧으면 검증 실패")
    void validation_fail_userNameTooShort() {
        // given
        UserSignupRequest dto = UserSignupRequest.builder()
                .userName("김")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequest>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("사용자 이름은 2자 이상 50자 이하여야 합니다.");
    }

    @Test
    @DisplayName("실패: 잘못된 이메일 형식이면 검증 실패")
    void validation_fail_invalidEmailFormat() {
        // given
        UserSignupRequest dto = UserSignupRequest.builder()
                .userName("홍길동")
                .email("invalid-email")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequest>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("올바른 이메일 형식이 아닙니다.");
    }

    @Test
    @DisplayName("실패: 휴대폰 번호가 010이 아니면 검증 실패")
    void validation_fail_userNumberNot010() {
        // given
        UserSignupRequest dto = UserSignupRequest.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("011-1234-5678")
                .password("Test123!")
                .verificationCode("123456")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequest>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("핸드폰 번호는 010-XXXX-XXXX 형식이어야 합니다.");
    }

    @Test
    @DisplayName("실패: 비밀번호가 너무 짧으면 검증 실패")
    void validation_fail_passwordTooShort() {
        // given
        UserSignupRequest dto = UserSignupRequest.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Te1!")  // 4자로 변경 (확실히 6자 미만)
                .verificationCode("123456")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequest>> violations = validator.validate(dto);

        // then: @Size와 @Pattern 검증 모두 실패할 수 있음
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(ConstraintViolation::getMessage))
                .containsAnyOf("비밀번호는 6자 이상 20자 이하여야 합니다.", 
                              "비밀번호는 6-20자의 대소문자, 숫자, 특수문자를 포함해야 합니다.");
    }

    @Test
    @DisplayName("실패: 비밀번호에 대문자가 없으면 검증 실패")
    void validation_fail_passwordNoUppercase() {
        // given
        UserSignupRequest dto = UserSignupRequest.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("test123!")
                .verificationCode("123456")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequest>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 6-20자의 대소문자, 숫자, 특수문자를 포함해야 합니다.");
    }

    @Test
    @DisplayName("실패: 인증 코드가 빈 값이면 검증 실패")
    void validation_fail_emptyVerificationCode() {
        // given
        UserSignupRequest dto = UserSignupRequest.builder()
                .userName("홍길동")
                .email("test@example.com")
                .userNumber("010-1234-5678")
                .password("Test123!")
                .verificationCode("")
                .build();

        // when
        Set<ConstraintViolation<UserSignupRequest>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("이메일 인증 코드는 필수입니다.");
    }
}
