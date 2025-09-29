package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

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
    @Mock
    private AuthService authService;

    private CustomOAuth2UserService testService;
    private OAuth2UserRequest userRequest;

    @BeforeEach
    void setUp() {
        Map<String, Object> oAuth2UserAttributes = new HashMap<>();
        oAuth2UserAttributes.put(USER_NAME_ATTRIBUTE_NAME, GOOGLE_USER_ID);
        oAuth2UserAttributes.put("email", USER_EMAIL);
        oAuth2UserAttributes.put("name", USER_NAME);

        ClientRegistration.ProviderDetails providerDetails = mock(ClientRegistration.ProviderDetails.class);
        ClientRegistration.ProviderDetails.UserInfoEndpoint userInfoEndpoint = mock(ClientRegistration.ProviderDetails.UserInfoEndpoint.class);
        given(userInfoEndpoint.getUserNameAttributeName()).willReturn(USER_NAME_ATTRIBUTE_NAME);
        given(providerDetails.getUserInfoEndpoint()).willReturn(userInfoEndpoint);

        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        given(clientRegistration.getRegistrationId()).willReturn(GOOGLE_REGISTRATION_ID);
        given(clientRegistration.getProviderDetails()).willReturn(providerDetails);

        userRequest = mock(OAuth2UserRequest.class);
        given(userRequest.getClientRegistration()).willReturn(clientRegistration);

        OAuth2User delegateLoadUserResult = new DefaultOAuth2User(Collections.emptyList(), oAuth2UserAttributes, USER_NAME_ATTRIBUTE_NAME);
        given(delegate.loadUser(userRequest)).willReturn(delegateLoadUserResult);

        testService = new CustomOAuth2UserService(userRepository, delegate, authService);
    }

    @Nested
    @DisplayName("기존 사용자 시나리오")
    class ExistingUserScenario {
        @Test
        @DisplayName("기존 사용자를 성공적으로 처리")
        void shouldProcessExistingUser() {
            // given
            User existingUser = mock(User.class);
            given(existingUser.getUserId()).willReturn(USER_ID);
            given(existingUser.getUserRole()).willReturn(User.UserRole.USER);

            given(userRepository.findByUserEmail(USER_EMAIL)).willReturn(Optional.of(existingUser));
            given(authService.integrateSocialAuth(any(User.class), any(UserAuth.AuthType.class), any()))
                    .willReturn(existingUser);

            // when
            OAuth2User result = testService.loadUser(userRequest);

            // then
            then(authService).should().integrateSocialAuth(existingUser, UserAuth.AuthType.GOOGLE, GOOGLE_USER_ID);
            assertThat(result.getAttributes().get("userId")).isEqualTo(USER_ID);
            assertThat(result.getAttributes().get("isExistingUser")).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("신규 사용자 시나리오")
    class NewUserScenario {
        @Test
        @DisplayName("신규 사용자를 성공적으로 처리")
        void shouldProcessNewUser() {
            // given
            given(userRepository.findByUserEmail(USER_EMAIL)).willReturn(Optional.empty());

            // when
            OAuth2User result = testService.loadUser(userRequest);

            // then
            then(authService).should(never()).integrateSocialAuth(any(), any(), any());
            assertThat(result.getAttributes().get("isExistingUser")).isEqualTo(false);
            assertThat(result.getAttributes()).doesNotContainKey("userId");
        }
    }
}