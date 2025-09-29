package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.auth.SocialSignupRequest;
import com.jober.final2teamdrhong.dto.userSignup.UserSignupRequest;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.exception.BusinessException;
import com.jober.final2teamdrhong.exception.DuplicateResourceException;
import com.jober.final2teamdrhong.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * UserValidationService 테스트
 * 실제 구현체의 동작을 정확히 반영한 테스트 코드
 */
@ExtendWith(MockitoExtension.class)
class UserValidationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidationService userValidationService;

    @Nested
    @DisplayName("이메일 중복 검증 테스트")
    class ValidateEmailDuplicationTest {

        @Test
        @DisplayName("중복되지 않은 이메일로 검증 성공 테스트")
        void shouldPassWhenEmailIsNotDuplicated() {
            // given
            // 1. 중복되지 않은 이메일을 준비합니다.
            String email = "unique@example.com";

            // 2. 이메일이 존재하지 않도록 설정합니다.
            given(userRepository.findByUserEmail(email)).willReturn(Optional.empty());

            // when & then
            // 1. 이메일 중복 검증이 예외 없이 통과하는지 확인합니다.
            assertDoesNotThrow(() -> userValidationService.validateEmailDuplication(email));

            // 2. UserRepository 조회가 호출되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmail(email);
        }

        @Test
        @DisplayName("이미 가입된 이메일로 검증 실패 테스트")
        void shouldFailWhenEmailIsDuplicated() {
            // given
            // 1. 이미 가입된 이메일을 준비합니다.
            String duplicatedEmail = "existing@example.com";

            // 2. 기존 사용자를 생성합니다.
            User existingUser = User.create("기존사용자", duplicatedEmail, "010-1234-5678");

            // 3. 이메일이 이미 존재하도록 설정합니다.
            given(userRepository.findByUserEmail(duplicatedEmail)).willReturn(Optional.of(existingUser));

            // when & then
            // 1. 이메일 중복으로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> userValidationService.validateEmailDuplication(duplicatedEmail))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("이미 가입된 이메일입니다.");

            // 2. UserRepository 조회가 호출되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmail(duplicatedEmail);
        }
    }

    @Nested
    @DisplayName("핸드폰 번호 중복 검증 테스트")
    class ValidatePhoneNumberDuplicationTest {

        @Test
        @DisplayName("핸드폰 번호 중복 검증 스킵 테스트 (미구현 기능)")
        void shouldSkipPhoneNumberValidation() {
            // given
            // 1. 임의의 핸드폰 번호를 준비합니다.
            String phoneNumber = "010-1234-5678";

            // when & then
            // 1. 핸드폰 번호 검증이 현재 스킵되므로 예외 없이 통과하는지 확인합니다.
            assertDoesNotThrow(() -> userValidationService.validatePhoneNumberDuplication(phoneNumber));

            // 2. 실제 구현체에서는 UserRepository 조회를 하지 않으므로 호출되지 않았는지 확인합니다.
            then(userRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("필수 약관 동의 검증 테스트")
    class ValidateRequiredAgreementsTest {

        @Test
        @DisplayName("모든 필수 약관에 동의했을 때 검증 성공 테스트")
        void shouldPassWhenAllRequiredAgreementsAreAccepted() {
            // given
            // 1. 모든 필수 약관에 동의한 상태를 준비합니다.
            boolean termsAgreed = true;
            boolean privacyAgreed = true;

            // when & then
            // 1. 필수 약관 동의 검증이 예외 없이 통과하는지 확인합니다.
            assertDoesNotThrow(() -> userValidationService.validateRequiredAgreements(termsAgreed, privacyAgreed));
        }

        @Test
        @DisplayName("이용약관에 동의하지 않았을 때 검증 실패 테스트")
        void shouldFailWhenTermsNotAgreed() {
            // given
            // 1. 이용약관에만 동의하지 않은 상태를 준비합니다.
            boolean termsAgreed = false;
            boolean privacyAgreed = true;

            // when & then
            // 1. 이용약관 미동의로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> userValidationService.validateRequiredAgreements(termsAgreed, privacyAgreed))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("필수 약관에 동의해야 합니다.");
        }

        @Test
        @DisplayName("개인정보처리방침에 동의하지 않았을 때 검증 실패 테스트")
        void shouldFailWhenPrivacyPolicyNotAgreed() {
            // given
            // 1. 개인정보처리방침에만 동의하지 않은 상태를 준비합니다.
            boolean termsAgreed = true;
            boolean privacyAgreed = false;

            // when & then
            // 1. 개인정보처리방침 미동의로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> userValidationService.validateRequiredAgreements(termsAgreed, privacyAgreed))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("필수 약관에 동의해야 합니다.");
        }

        @Test
        @DisplayName("모든 필수 약관에 동의하지 않았을 때 검증 실패 테스트")
        void shouldFailWhenNoRequiredAgreementsAccepted() {
            // given
            // 1. 모든 필수 약관에 동의하지 않은 상태를 준비합니다.
            boolean termsAgreed = false;
            boolean privacyAgreed = false;

            // when & then
            // 1. 모든 약관 미동의로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> userValidationService.validateRequiredAgreements(termsAgreed, privacyAgreed))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("필수 약관에 동의해야 합니다.");
        }
    }

    @Nested
    @DisplayName("로컬 회원가입 비즈니스 규칙 검증 테스트")
    class ValidateLocalSignupBusinessRulesTest {

        @Test
        @DisplayName("유효한 로컬 회원가입 요청으로 검증 성공 테스트")
        void shouldPassWithValidLocalSignupRequest() {
            // given
            // 1. 유효한 로컬 회원가입 요청을 준비합니다.
            UserSignupRequest request = new UserSignupRequest(
                    "테스트사용자",
                    "new@example.com",
                    "010-1234-5678",
                    "Password123!",
                    "123456"
            );

            // 2. 이메일이 중복되지 않도록 설정합니다.
            given(userRepository.findByUserEmail(request.email())).willReturn(Optional.empty());

            // when & then
            // 1. 로컬 회원가입 비즈니스 규칙 검증이 예외 없이 통과하는지 확인합니다.
            assertDoesNotThrow(() -> userValidationService.validateLocalSignupBusinessRules(request));

            // 2. 이메일 중복 검사가 호출되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmail(request.email());
        }

        @Test
        @DisplayName("이메일이 중복된 로컬 회원가입 요청으로 검증 실패 테스트")
        void shouldFailWithDuplicatedEmailInLocalSignup() {
            // given
            // 1. 중복된 이메일을 가진 로컬 회원가입 요청을 준비합니다.
            UserSignupRequest request = new UserSignupRequest(
                    "테스트사용자",
                    "existing@example.com",
                    "010-1234-5678",
                    "Password123!",
                    "123456"
            );

            // 2. 기존 사용자를 생성합니다.
            User existingUser = User.create("기존사용자", "existing@example.com", "010-9999-9999");

            // 3. 이메일이 이미 존재하도록 설정합니다.
            given(userRepository.findByUserEmail(request.email())).willReturn(Optional.of(existingUser));

            // when & then
            // 1. 이메일 중복으로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> userValidationService.validateLocalSignupBusinessRules(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("이미 가입된 이메일입니다.");

            // 2. 이메일 중복 검사가 호출되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmail(request.email());
        }
    }

    @Nested
    @DisplayName("소셜 회원가입 비즈니스 규칙 검증 테스트")
    class ValidateSocialSignupBusinessRulesTest {

        @Test
        @DisplayName("유효한 소셜 회원가입 요청으로 검증 성공 테스트")
        void shouldPassWithValidSocialSignupRequest() {
            // given
            // 1. 유효한 소셜 회원가입 정보를 준비합니다.
            String email = "social@example.com";
            String name = "소셜사용자";
            SocialSignupRequest request = new SocialSignupRequest(
                    "google",           // provider
                    "google_123",       // socialId
                    email,              // email
                    name,               // name
                    "010-1234-5678",    // phoneNumber
                    true,               // agreedToTerms
                    true,               // agreedToPrivacyPolicy
                    false               // agreedToMarketing
            );

            // 2. 이메일이 중복되지 않도록 설정합니다.
            given(userRepository.findByUserEmail(email)).willReturn(Optional.empty());

            // when & then
            // 1. 소셜 회원가입 비즈니스 규칙 검증이 예외 없이 통과하는지 확인합니다.
            assertDoesNotThrow(() -> userValidationService.validateSocialSignupBusinessRules(email, name, request));

            // 2. 이메일 중복 검사가 호출되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmail(email);
        }

        @Test
        @DisplayName("이메일이 중복된 소셜 회원가입 요청으로 검증 실패 테스트")
        void shouldFailWithDuplicatedEmailInSocialSignup() {
            // given
            // 1. 중복된 이메일을 가진 소셜 회원가입 정보를 준비합니다.
            String email = "existing@example.com";
            String name = "중복사용자";
            SocialSignupRequest request = new SocialSignupRequest(
                    "google",           // provider
                    "google_123",       // socialId
                    email,              // email
                    name,               // name
                    "010-1234-5678",    // phoneNumber
                    true,               // agreedToTerms
                    true,               // agreedToPrivacyPolicy
                    false               // agreedToMarketing
            );

            // 2. 기존 사용자를 생성합니다.
            User existingUser = User.create("기존사용자", email, "010-9999-9999");

            // 3. 이메일이 이미 존재하도록 설정합니다.
            given(userRepository.findByUserEmail(email)).willReturn(Optional.of(existingUser));

            // when & then
            // 1. 이메일 중복으로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> userValidationService.validateSocialSignupBusinessRules(email, name, request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("이미 가입된 이메일입니다.");

            // 2. 이메일 중복 검사가 호출되었는지 확인합니다.
            then(userRepository).should(times(1)).findByUserEmail(email);
        }

        @Test
        @DisplayName("이용약관에 동의하지 않은 소셜 회원가입 요청으로 검증 실패 테스트")
        void shouldFailWithTermsNotAgreedInSocialSignup() {
            // given
            // 1. 이용약관에 동의하지 않은 소셜 회원가입 정보를 준비합니다.
            String email = "social@example.com";
            String name = "소셜사용자";
            SocialSignupRequest request = new SocialSignupRequest(
                    "google",           // provider
                    "google_123",       // socialId
                    email,              // email
                    name,               // name
                    "010-1234-5678",    // phoneNumber
                    false,              // agreedToTerms (미동의)
                    true,               // agreedToPrivacyPolicy
                    false               // agreedToMarketing
            );

            // 2. 이메일이 중복되지 않도록 설정합니다.
            given(userRepository.findByUserEmail(email)).willReturn(Optional.empty());

            // when & then
            // 1. 이용약관 미동의로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> userValidationService.validateSocialSignupBusinessRules(email, name, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("필수 약관에 동의해야 합니다.");

            // 2. 이메일 중복 검사는 호출되었는지 확인합니다 (검증 순서상 먼저 실행됨).
            then(userRepository).should(times(1)).findByUserEmail(email);
        }

        @Test
        @DisplayName("개인정보처리방침에 동의하지 않은 소셜 회원가입 요청으로 검증 실패 테스트")
        void shouldFailWithPrivacyPolicyNotAgreedInSocialSignup() {
            // given
            // 1. 개인정보처리방침에 동의하지 않은 소셜 회원가입 정보를 준비합니다.
            String email = "social@example.com";
            String name = "소셜사용자";
            SocialSignupRequest request = new SocialSignupRequest(
                    "google",           // provider
                    "google_123",       // socialId
                    email,              // email
                    name,               // name
                    "010-1234-5678",    // phoneNumber
                    true,               // agreedToTerms
                    false,              // agreedToPrivacyPolicy (미동의)
                    false               // agreedToMarketing
            );

            // 2. 이메일이 중복되지 않도록 설정합니다.
            given(userRepository.findByUserEmail(email)).willReturn(Optional.empty());

            // when & then
            // 1. 개인정보처리방침 미동의로 인한 예외가 발생하는지 확인합니다.
            assertThatThrownBy(() -> userValidationService.validateSocialSignupBusinessRules(email, name, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("필수 약관에 동의해야 합니다.");

            // 2. 이메일 중복 검사는 호출되었는지 확인합니다 (검증 순서상 먼저 실행됨).
            then(userRepository).should(times(1)).findByUserEmail(email);
        }
    }

    @Nested
    @DisplayName("계정 통합 가능성 검사 테스트")
    class CanMergeWithExistingAccountTest {

        @Test
        @DisplayName("계정 통합 가능성 검사 스킵 테스트 (미구현 기능)")
        void shouldSkipAccountMergeCheck() {
            // given
            // 1. 임의의 이메일과 핸드폰 번호를 준비합니다.
            String email = "test@example.com";
            String phoneNumber = "010-1234-5678";

            // when
            // 1. 계정 통합 가능성을 검사합니다.
            boolean result = userValidationService.canMergeWithExistingAccount(email, phoneNumber);

            // then
            // 1. 미구현 기능이므로 항상 false를 반환하는지 확인합니다.
            assertThat(result).isFalse();

            // 2. 실제 구현체에서는 UserRepository 조회를 하지 않으므로 호출되지 않았는지 확인합니다.
            then(userRepository).shouldHaveNoInteractions();
        }
    }
}