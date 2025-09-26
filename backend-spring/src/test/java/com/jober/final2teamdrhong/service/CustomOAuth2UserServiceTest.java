package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.auth.OAuth2UserInfo;
import com.jober.final2teamdrhong.dto.auth.OAuth2UserInfoFactory;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.exception.BusinessException;
import com.jober.final2teamdrhong.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * CustomOAuth2UserService 포괄적 테스트
 * 실제 구현의 모든 핵심 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    // 테스트에 사용할 상수들
    private static final String GOOGLE_REGISTRATION_ID = "google";
    private static final String USER_NAME_ATTRIBUTE_NAME = "sub";
    private static final String GOOGLE_USER_ID = "google123";
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_NAME = "Test User";
    private static final Integer USER_ID = 1;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DefaultOAuth2UserService delegate;

    private CustomOAuth2UserService testService; 

    // 테스트용 데이터
    private OAuth2UserRequest userRequest;
    private Map<String, Object> oAuth2UserAttributes;
    private OAuth2UserInfo oAuth2UserInfo;
    private User existingUser;

    @BeforeEach
    void setUp() {
        // OAuth2User 속성 맵 생성
        oAuth2UserAttributes = new HashMap<>();
        oAuth2UserAttributes.put("sub", GOOGLE_USER_ID);
        oAuth2UserAttributes.put("email", USER_EMAIL);
        oAuth2UserAttributes.put("name", USER_NAME);

        // ClientRegistration Mock 생성
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(GOOGLE_REGISTRATION_ID)
                .clientId("google-client-id")
                .clientSecret("google-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(USER_NAME_ATTRIBUTE_NAME)
                .build();

        // OAuth2UserRequest Mock 생성
        userRequest = mock(OAuth2UserRequest.class);
        lenient().when(userRequest.getClientRegistration()).thenReturn(clientRegistration);

        // delegate.loadUser() 결과 Mock 생성
        OAuth2User delegateLoadUserResult = new DefaultOAuth2User(
                Collections.emptyList(),
                oAuth2UserAttributes,
                USER_NAME_ATTRIBUTE_NAME
        );

        // delegate Mock 설정
        lenient().when(delegate.loadUser(userRequest)).thenReturn(delegateLoadUserResult);

        // 컴포지션을 사용한 테스트 서비스 생성
        testService = new CustomOAuth2UserService(userRepository, delegate);

        // OAuth2UserInfo Mock 생성
        oAuth2UserInfo = mock(OAuth2UserInfo.class);
        lenient().when(oAuth2UserInfo.getId()).thenReturn(GOOGLE_USER_ID);
        lenient().when(oAuth2UserInfo.getEmail()).thenReturn(USER_EMAIL);
        lenient().when(oAuth2UserInfo.getName()).thenReturn(USER_NAME);
        lenient().when(oAuth2UserInfo.getProvider()).thenReturn("google");
        lenient().when(oAuth2UserInfo.getAttributes()).thenReturn(oAuth2UserAttributes);

        // 기존 사용자 Mock 생성
        existingUser = createMockUser();
    }

    @Nested
    @DisplayName("OAuth2 사용자 로드 - 기존 사용자 시나리오")
    class ExistingUserScenarioTest {

        @Test
        @DisplayName("기존 사용자이고 해당 OAuth2 인증이 이미 연결된 경우")
        void shouldHandleExistingUserWithExistingOAuth2Auth() {
            // given
            // 기존 UserAuth 설정 - 이미 GOOGLE로 연결되어 있음
            UserAuth existingAuth = createMockUserAuth(UserAuth.AuthType.GOOGLE, GOOGLE_USER_ID);
            List<UserAuth> userAuths = new ArrayList<>();
            userAuths.add(existingAuth);
            given(existingUser.getUserAuths()).willReturn(userAuths);

            given(userRepository.findByUserEmail(USER_EMAIL)).willReturn(Optional.of(existingUser));

            try (MockedStatic<OAuth2UserInfoFactory> factoryMock = mockStatic(OAuth2UserInfoFactory.class)) {
                factoryMock.when(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(
                        eq(GOOGLE_REGISTRATION_ID), any())).thenReturn(oAuth2UserInfo);
                factoryMock.when(() -> OAuth2UserInfoFactory.getAuthType(GOOGLE_REGISTRATION_ID))
                        .thenReturn(UserAuth.AuthType.GOOGLE);

                // when
                OAuth2User result = testService.loadUser(userRequest);

                // then
                assertThat(result.getAttributes()).containsEntry("isExistingUser", true);
                assertThat(result.getAttributes()).containsEntry("provider", "google");
                assertThat(result.getAttributes()).containsEntry("userId", USER_ID);
                assertThat(result.getAttributes()).containsEntry("userRole", "USER");
                assertThat(result.getAuthorities()).hasSize(1);
                assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

                // UserAuth 추가가 호출되지 않았는지 확인 (이미 존재하므로)
                then(existingUser).should(never()).addUserAuth(any(UserAuth.class));

                // delegate.loadUser()가 호출되었는지 확인
                then(delegate).should(times(1)).loadUser(userRequest);
            }
        }
        @Test
        @DisplayName("기존 사용자이지만 해당 OAuth2 인증이 연결되지 않은 경우 - 새 인증 추가")
        void shouldAddNewOAuth2AuthToExistingUser() {
            // given
            // 다른 종류의 기존 UserAuth만 설정 (GOOGLE이 아닌)
            UserAuth existingAuth = createMockUserAuth(UserAuth.AuthType.LOCAL, null);
            List<UserAuth> userAuths = new ArrayList<>();
            userAuths.add(existingAuth);
            given(existingUser.getUserAuths()).willReturn(userAuths);

            given(userRepository.findByUserEmail(USER_EMAIL)).willReturn(Optional.of(existingUser));

            try (MockedStatic<OAuth2UserInfoFactory> factoryMock = mockStatic(OAuth2UserInfoFactory.class);
                 MockedStatic<UserAuth> userAuthMock = mockStatic(UserAuth.class)) {

                factoryMock.when(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(
                        eq(GOOGLE_REGISTRATION_ID), any())).thenReturn(oAuth2UserInfo);
                factoryMock.when(() -> OAuth2UserInfoFactory.getAuthType(GOOGLE_REGISTRATION_ID))
                        .thenReturn(UserAuth.AuthType.GOOGLE);

                UserAuth newAuth = createMockUserAuth(UserAuth.AuthType.GOOGLE, GOOGLE_USER_ID);
                userAuthMock.when(() -> UserAuth.createSocialAuth(existingUser, UserAuth.AuthType.GOOGLE, GOOGLE_USER_ID))
                        .thenReturn(newAuth);

                // when
                OAuth2User result = testService.loadUser(userRequest);

                // then
                assertThat(result.getAttributes()).containsEntry("isExistingUser", true);
                assertThat(result.getAttributes()).containsEntry("provider", "google");
                assertThat(result.getAttributes()).containsEntry("userId", USER_ID);
                assertThat(result.getAuthorities()).hasSize(1);

                // 새로운 UserAuth가 추가되었는지 확인
                then(existingUser).should(times(1)).addUserAuth(newAuth);

                // delegate.loadUser()가 호출되었는지 확인
                then(delegate).should(times(1)).loadUser(userRequest);
            }
        }
    }

    @Nested
    @DisplayName("OAuth2 사용자 로드 - 신규 사용자 시나리오")
    class NewUserScenarioTest {

        @Test
        @DisplayName("신규 사용자인 경우 임시 OAuth2User 생성")
        void shouldCreateTemporaryOAuth2UserForNewUser() {
            // given
            given(userRepository.findByUserEmail(USER_EMAIL)).willReturn(Optional.empty());

            try (MockedStatic<OAuth2UserInfoFactory> factoryMock = mockStatic(OAuth2UserInfoFactory.class)) {
                factoryMock.when(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(
                        eq(GOOGLE_REGISTRATION_ID), any())).thenReturn(oAuth2UserInfo);

                // when
                OAuth2User result = testService.loadUser(userRequest);

                // then
                assertThat(result.getAttributes()).containsEntry("isExistingUser", false);
                assertThat(result.getAttributes()).containsEntry("provider", "google");
                assertThat(result.getAttributes()).doesNotContainKey("userId");
                assertThat(result.getAttributes()).doesNotContainKey("userRole");
                assertThat(result.getAuthorities()).isEmpty(); // 신규 사용자는 권한 없음

                // 데이터베이스 저장이 호출되지 않았는지 확인
                then(userRepository).should(never()).save(any(User.class));

                // delegate.loadUser()가 호출되었는지 확인
                then(delegate).should(times(1)).loadUser(userRequest);
            }
        }
    }

    @Nested
    @DisplayName("OAuth2 사용자 정보 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("OAuth2 사용자 ID가 없으면 BusinessException 발생")
        void shouldThrowBusinessExceptionWhenUserIdIsEmpty() {
            // given
            OAuth2UserInfo invalidUserInfo = mock(OAuth2UserInfo.class);
            lenient().when(invalidUserInfo.getId()).thenReturn(""); // ID를 비움
            lenient().when(invalidUserInfo.getEmail()).thenReturn(USER_EMAIL);
            lenient().when(invalidUserInfo.getName()).thenReturn(USER_NAME);

            try (MockedStatic<OAuth2UserInfoFactory> factoryMock = mockStatic(OAuth2UserInfoFactory.class)) {
                factoryMock.when(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(
                        eq(GOOGLE_REGISTRATION_ID), any())).thenReturn(invalidUserInfo);

                // when & then
                assertThatThrownBy(() -> testService.loadUser(userRequest))
                        .isInstanceOf(OAuth2AuthenticationException.class)
                        .hasCauseInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("OAuth2 사용자 이메일이 없으면 BusinessException 발생")
        void shouldThrowBusinessExceptionWhenUserEmailIsEmpty() {
            // given
            OAuth2UserInfo invalidUserInfo = mock(OAuth2UserInfo.class);
            lenient().when(invalidUserInfo.getId()).thenReturn(GOOGLE_USER_ID);
            lenient().when(invalidUserInfo.getEmail()).thenReturn(null); // 이메일을 null로 설정
            lenient().when(invalidUserInfo.getName()).thenReturn(USER_NAME);

            try (MockedStatic<OAuth2UserInfoFactory> factoryMock = mockStatic(OAuth2UserInfoFactory.class)) {
                factoryMock.when(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(
                        eq(GOOGLE_REGISTRATION_ID), any())).thenReturn(invalidUserInfo);

                // when & then
                assertThatThrownBy(() -> testService.loadUser(userRequest))
                        .isInstanceOf(OAuth2AuthenticationException.class)
                        .hasCauseInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("OAuth2 사용자 이름이 없으면 BusinessException 발생")
        void shouldThrowBusinessExceptionWhenUserNameIsEmpty() {
            // given
            OAuth2UserInfo invalidUserInfo = mock(OAuth2UserInfo.class);
            lenient().when(invalidUserInfo.getId()).thenReturn(GOOGLE_USER_ID);
            lenient().when(invalidUserInfo.getEmail()).thenReturn(USER_EMAIL);
            lenient().when(invalidUserInfo.getName()).thenReturn("   "); // 이름은 공백으로 설정

            try (MockedStatic<OAuth2UserInfoFactory> factoryMock = mockStatic(OAuth2UserInfoFactory.class)) {
                factoryMock.when(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(
                        eq(GOOGLE_REGISTRATION_ID), any())).thenReturn(invalidUserInfo);

                // when & then
                assertThatThrownBy(() -> testService.loadUser(userRequest))
                        .isInstanceOf(OAuth2AuthenticationException.class)
                        .hasCauseInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("OAuth2UserInfoFactory에서 예외 발생 시 OAuth2AuthenticationException으로 래핑")
        void shouldWrapExceptionFromOAuth2UserInfoFactory() {
            // given
            try (MockedStatic<OAuth2UserInfoFactory> factoryMock = mockStatic(OAuth2UserInfoFactory.class)) {
                factoryMock.when(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(
                        eq(GOOGLE_REGISTRATION_ID), any()))
                        .thenThrow(new RuntimeException("Factory error"));

                // when & then
                assertThatThrownBy(() -> testService.loadUser(userRequest))
                        .isInstanceOf(OAuth2AuthenticationException.class);
            }
        }

        @Test
        @DisplayName("UserRepository에서 예외 발생 시 OAuth2AuthenticationException으로 래핑")
        void shouldWrapExceptionFromUserRepository() {
            // given
            given(userRepository.findByUserEmail(USER_EMAIL))
                    .willThrow(new RuntimeException("Database error"));

            try (MockedStatic<OAuth2UserInfoFactory> factoryMock = mockStatic(OAuth2UserInfoFactory.class)) {
                factoryMock.when(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(
                        eq(GOOGLE_REGISTRATION_ID), any())).thenReturn(oAuth2UserInfo);

                // when & then
                assertThatThrownBy(() -> testService.loadUser(userRequest))
                        .isInstanceOf(OAuth2AuthenticationException.class);
            }
        }
    }

    // ==================== 헬퍼 메서드 ====================

    private User createMockUser() {
        User mockUser = mock(User.class);
        lenient().when(mockUser.getUserId()).thenReturn(USER_ID);
        lenient().when(mockUser.getUserEmail()).thenReturn(USER_EMAIL);
        lenient().when(mockUser.getUserRole()).thenReturn(User.UserRole.USER);
        return mockUser;
    }

    private UserAuth createMockUserAuth(UserAuth.AuthType authType, String socialId) {
        UserAuth mockAuth = mock(UserAuth.class);
        lenient().when(mockAuth.getAuthType()).thenReturn(authType);
        lenient().when(mockAuth.getSocialId()).thenReturn(socialId);
        return mockAuth;
    }
}
