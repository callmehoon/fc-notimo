package com.jober.final2teamdrhong.dto.auth;

import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.exception.BusinessException;

import java.util.Map;

/**
 * OAuth2 제공자별 사용자 정보 객체 생성을 담당하는 팩토리 클래스
 * Thread-safe한 정적 팩토리 메서드를 제공합니다.
 */
public class OAuth2UserInfoFactory {

    /**
     * OAuth2 제공자에 따라 적절한 OAuth2UserInfo 구현체를 반환
     *
     * @param registrationId OAuth2 제공자 ID (google, naver, kakao) - null이나 빈 문자열 불가
     * @param attributes 사용자 속성 정보 - null 불가
     * @return OAuth2UserInfo 구현체
     * @throws BusinessException 파라미터가 유효하지 않거나 지원하지 않는 제공자인 경우
     */
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        // 통합된 파라미터 검증 (BusinessException으로 통일)
        validateParameters(registrationId, attributes);

        UserAuth.AuthType authType = convertToAuthType(registrationId);

        return switch (authType) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case NAVER -> throw new BusinessException("네이버 로그인은 아직 지원하지 않습니다.");
            case KAKAO -> throw new BusinessException("카카오 로그인은 아직 지원하지 않습니다.");
            default -> throw new BusinessException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
        };
    }

    /**
     * registrationId를 AuthType으로 변환 (외부 사용을 위한 public 메서드)
     *
     * @param registrationId OAuth2 제공자 ID - null이나 빈 문자열 불가
     * @return AuthType
     * @throws BusinessException 파라미터가 유효하지 않거나 지원하지 않는 제공자인 경우
     */
    public static UserAuth.AuthType getAuthType(String registrationId) {
        validateRegistrationId(registrationId);
        return convertToAuthType(registrationId);
    }

    /**
     * registrationId를 AuthType으로 변환 (내부 사용)
     * 파라미터 검증은 이미 완료되었다고 가정
     */
    private static UserAuth.AuthType convertToAuthType(String registrationId) {
        UserAuth.AuthType authType = UserAuth.AuthType.fromProvider(registrationId);
        if (authType == null || authType == UserAuth.AuthType.LOCAL) {
            throw new BusinessException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
        }
        return authType;
    }

    /**
     * OAuth2UserInfo 생성을 위한 파라미터 유효성 검증
     *
     * @param registrationId OAuth2 제공자 ID
     * @param attributes 사용자 속성 정보
     * @throws BusinessException 파라미터가 유효하지 않은 경우
     */
    private static void validateParameters(String registrationId, Map<String, Object> attributes) {
        validateRegistrationId(registrationId);

        if (attributes == null) {
            throw new BusinessException("사용자 속성 정보는 null일 수 없습니다.");
        }

        if (attributes.isEmpty()) {
            throw new BusinessException("사용자 속성 정보가 비어있습니다.");
        }
    }

    /**
     * registrationId 단독 검증
     *
     * @param registrationId OAuth2 제공자 ID
     * @throws BusinessException registrationId가 유효하지 않은 경우
     */
    private static void validateRegistrationId(String registrationId) {
        if (registrationId == null) {
            throw new BusinessException("OAuth2 제공자 ID는 null일 수 없습니다.");
        }

        if (registrationId.trim().isEmpty()) {
            throw new BusinessException("OAuth2 제공자 ID는 빈 문자열일 수 없습니다.");
        }
    }

    /**
     * 지원되는 OAuth2 제공자인지 확인
     *
     * @param registrationId OAuth2 제공자 ID
     * @return 지원 여부 (null이나 빈 문자열인 경우 false)
     */
    public static boolean isSupported(String registrationId) {
        try {
            validateRegistrationId(registrationId);
            UserAuth.AuthType authType = UserAuth.AuthType.fromProvider(registrationId);
            return authType != null && authType != UserAuth.AuthType.LOCAL && authType.isEnabled();
        } catch (BusinessException e) {
            return false; // 유효하지 않은 registrationId
        }
    }

    /**
     * 팩토리 클래스이므로 인스턴스 생성을 방지
     */
    private OAuth2UserInfoFactory() {
        throw new UnsupportedOperationException("유틸리티 클래스는 인스턴스를 생성할 수 없습니다.");
    }
}