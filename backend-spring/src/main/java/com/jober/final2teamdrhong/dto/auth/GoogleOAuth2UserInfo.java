package com.jober.final2teamdrhong.dto.auth;

import java.util.Map;

/**
 * Google OAuth2 사용자 정보 구현체
 * Google에서 제공하는 사용자 정보를 처리하는 클래스
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        // Google의 고유 사용자 ID (sub 필드)
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        // Google 사용자 이름 (name 필드)
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        // Google 이메일 (email 필드)
        return (String) attributes.get("email");
    }

    @Override
    public String getProvider() {
        return "google";
    }

}