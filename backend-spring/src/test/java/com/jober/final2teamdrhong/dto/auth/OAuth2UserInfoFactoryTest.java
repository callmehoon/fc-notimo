package com.jober.final2teamdrhong.dto.auth;

import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OAuth2UserInfoFactory 테스트
 * OAuth2 제공자별 사용자 정보 객체 생성 팩토리 클래스의 핵심 로직을 검증합니다.
 */
@DisplayName("OAuth2UserInfoFactory 테스트")
class OAuth2UserInfoFactoryTest {

    @Nested
    @DisplayName("getOAuth2UserInfo 메서드 테스트")
    class GetOAuth2UserInfoTest {

        @Test
        @DisplayName("Google 제공자로 GoogleOAuth2UserInfo 객체 생성 성공")
        void shouldCreateGoogleOAuth2UserInfoForGoogleProvider() {
            // given
            String registrationId = "google";
            Map<String, Object> attributes = createGoogleAttributes();

            // when
            OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

            // then
            assertThat(result).isInstanceOf(GoogleOAuth2UserInfo.class);
            assertThat(result.getId()).isEqualTo("google123");
            assertThat(result.getEmail()).isEqualTo("test@gmail.com");
            assertThat(result.getName()).isEqualTo("홍길동");
            assertThat(result.getProvider()).isEqualTo("google");
            assertThat(result.getAttributes()).isEqualTo(attributes);
        }

        @Test
        @DisplayName("빈 attributes로 Google 객체 생성 시 BusinessException 발생")
        void shouldThrowBusinessExceptionForEmptyAttributes() {
            // given
            String registrationId = "google";
            Map<String, Object> emptyAttributes = new HashMap<>();

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, emptyAttributes))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("사용자 속성 정보가 비어있습니다.");
        }

        @Test
        @DisplayName("NAVER 제공자는 아직 지원하지 않음 - BusinessException 발생")
        void shouldThrowBusinessExceptionForNaverProvider() {
            // given
            String registrationId = "naver";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", "test"); // 빈 attributes 방지

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("네이버 로그인은 아직 지원하지 않습니다.");
        }

        @Test
        @DisplayName("KAKAO 제공자는 아직 지원하지 않음 - BusinessException 발생")
        void shouldThrowBusinessExceptionForKakaoProvider() {
            // given
            String registrationId = "kakao";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", "test"); // 빈 attributes 방지

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("카카오 로그인은 아직 지원하지 않습니다.");
        }

        @Test
        @DisplayName("지원하지 않는 제공자는 BusinessException 발생")
        void shouldThrowBusinessExceptionForUnsupportedProvider() {
            // given
            String registrationId = "github";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", "test"); // 빈 attributes 방지

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("지원하지 않는 OAuth2 제공자입니다: github");
        }

        @Test
        @DisplayName("null registrationId로 BusinessException 발생")
        void shouldThrowBusinessExceptionForNullRegistrationId() {
            // given
            String registrationId = null;
            Map<String, Object> attributes = new HashMap<>();

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("OAuth2 제공자 ID는 null일 수 없습니다.");
        }

        @Test
        @DisplayName("빈 문자열 registrationId로 BusinessException 발생")
        void shouldThrowBusinessExceptionForEmptyRegistrationId() {
            // given
            String registrationId = "";
            Map<String, Object> attributes = new HashMap<>();

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("OAuth2 제공자 ID는 빈 문자열일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("getAuthType 메서드 테스트")
    class GetAuthTypeTest {

        @Test
        @DisplayName("google registrationId로 GOOGLE AuthType 반환")
        void shouldReturnGoogleAuthTypeForGoogleRegistrationId() {
            // given
            String registrationId = "google";

            // when
            UserAuth.AuthType result = OAuth2UserInfoFactory.getAuthType(registrationId);

            // then
            assertThat(result).isEqualTo(UserAuth.AuthType.GOOGLE);
        }

        @Test
        @DisplayName("naver registrationId로 NAVER AuthType 반환")
        void shouldReturnNaverAuthTypeForNaverRegistrationId() {
            // given
            String registrationId = "naver";

            // when
            UserAuth.AuthType result = OAuth2UserInfoFactory.getAuthType(registrationId);

            // then
            assertThat(result).isEqualTo(UserAuth.AuthType.NAVER);
        }

        @Test
        @DisplayName("kakao registrationId로 KAKAO AuthType 반환")
        void shouldReturnKakaoAuthTypeForKakaoRegistrationId() {
            // given
            String registrationId = "kakao";

            // when
            UserAuth.AuthType result = OAuth2UserInfoFactory.getAuthType(registrationId);

            // then
            assertThat(result).isEqualTo(UserAuth.AuthType.KAKAO);
        }

        @Test
        @DisplayName("대소문자 섞인 GOOGLE registrationId도 올바르게 처리")
        void shouldHandleMixedCaseGoogleRegistrationId() {
            // given - fromProvider가 대소문자를 toUpperCase()로 처리함
            String registrationId = "Google";

            // when
            UserAuth.AuthType result = OAuth2UserInfoFactory.getAuthType(registrationId);

            // then
            assertThat(result).isEqualTo(UserAuth.AuthType.GOOGLE);
        }

        @Test
        @DisplayName("지원하지 않는 registrationId로 BusinessException 발생")
        void shouldThrowBusinessExceptionForUnsupportedRegistrationId() {
            // given
            String registrationId = "github";

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getAuthType(registrationId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("지원하지 않는 OAuth2 제공자입니다: github");
        }

        @Test
        @DisplayName("LOCAL AuthType은 OAuth2 제공자가 아니므로 BusinessException 발생")
        void shouldThrowBusinessExceptionForLocalAuthType() {
            // given
            String registrationId = "local";

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getAuthType(registrationId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("지원하지 않는 OAuth2 제공자입니다: local");
        }

        @Test
        @DisplayName("null registrationId로 BusinessException 발생")
        void shouldThrowBusinessExceptionForNullRegistrationIdInGetAuthType() {
            // given
            String registrationId = null;

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getAuthType(registrationId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("OAuth2 제공자 ID는 null일 수 없습니다.");
        }

        @Test
        @DisplayName("빈 문자열 registrationId로 BusinessException 발생")
        void shouldThrowBusinessExceptionForEmptyRegistrationIdInGetAuthType() {
            // given
            String registrationId = "";

            // when & then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getAuthType(registrationId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("OAuth2 제공자 ID는 빈 문자열일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("실제 Google OAuth2 응답과 유사한 데이터로 전체 플로우 테스트")
        void shouldHandleRealGoogleOAuth2ResponseLikeData() {
            // given - 실제 Google OAuth2 응답과 유사한 데이터
            String registrationId = "google";
            Map<String, Object> realGoogleAttributes = new HashMap<>();
            realGoogleAttributes.put("sub", "108394509634023847362");
            realGoogleAttributes.put("name", "김철수");
            realGoogleAttributes.put("given_name", "철수");
            realGoogleAttributes.put("family_name", "김");
            realGoogleAttributes.put("picture", "https://lh3.googleusercontent.com/a/default-user");
            realGoogleAttributes.put("email", "kim.cheolsu@gmail.com");
            realGoogleAttributes.put("email_verified", true);
            realGoogleAttributes.put("locale", "ko");

            // when
            OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, realGoogleAttributes);

            // then
            assertThat(result).isInstanceOf(GoogleOAuth2UserInfo.class);
            assertThat(result.getId()).isEqualTo("108394509634023847362");
            assertThat(result.getName()).isEqualTo("김철수");
            assertThat(result.getEmail()).isEqualTo("kim.cheolsu@gmail.com");
            assertThat(result.getProvider()).isEqualTo("google");

            // attributes 전체가 올바르게 저장되었는지 확인
            assertThat(result.getAttributes()).containsEntry("email_verified", true);
            assertThat(result.getAttributes()).containsEntry("locale", "ko");
        }
    }

    // ==================== 헬퍼 메서드 ====================

    private Map<String, Object> createGoogleAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google123");
        attributes.put("email", "test@gmail.com");
        attributes.put("name", "홍길동");
        attributes.put("given_name", "길동");
        attributes.put("family_name", "홍");
        attributes.put("picture", "https://example.com/picture.jpg");
        attributes.put("email_verified", true);
        return attributes;
    }
}