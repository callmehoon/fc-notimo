package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.auth.SocialSignupRequest;
import com.jober.final2teamdrhong.dto.auth.SocialSignupResponse;
import com.jober.final2teamdrhong.entity.UserAuth;
import com.jober.final2teamdrhong.exception.ErrorResponse;
import com.jober.final2teamdrhong.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 소셜 로그인 관련 API를 제공하는 컨트롤러
 * OAuth2 소셜 로그인 및 회원가입 완료 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/auth/social")
@RequiredArgsConstructor
@Tag(name = "소셜 인증", description = "OAuth2 소셜 로그인 관련 API")
public class SocialAuthController {

    private final AuthService authService;

    /**
     * 소셜 로그인 시작 (구글)
     * 클라이언트를 Google OAuth2 인증 페이지로 리다이렉트합니다.
     *
     * @return 구글 OAuth2 인증 URL로 리다이렉트
     */
    @GetMapping("/login/google")
    @Operation(
            summary = "구글 소셜 로그인 시작",
            description = "구글 OAuth2 인증 페이지로 리다이렉트하여 소셜 로그인을 시작합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "구글 OAuth2 인증 페이지로 리다이렉트"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<Void> loginWithGoogle() {
        // Spring Security OAuth2가 자동으로 처리하는 URL로 리다이렉트
        // context-path를 포함한 전체 경로로 리다이렉트
        return ResponseEntity.status(302)
                .header("Location", "/api/oauth2/authorization/google")
                .build();
    }

    /**
     * 내부용: 소셜 로그인 제공자 목록 조회
     * 프론트엔드에서 소셜 로그인 버튼 동적 생성을 위한 내부 API
     */
    @GetMapping("/providers")
    public ResponseEntity<?> getSupportedProviders() {
        // AuthType enum에서 지원하는 소셜 제공자 정보 가져오기
        var providerMap = new java.util.LinkedHashMap<String, Object>();

        for (UserAuth.AuthType authType : UserAuth.AuthType.getSupportedSocialProviders()) {
            String providerKey = authType.name().toLowerCase();
            providerMap.put(providerKey, java.util.Map.of(
                    "name", authType.getDisplayName(),
                    "loginUrl", authType.getLoginUrl(),
                    "fullUrl", authType.getLoginUrl(), // 프론트엔드에서 baseURL 조합
                    "icon", authType.getIconUrl(),
                    "description", authType.getDescription(),
                    "enabled", authType.isEnabled(),
                    "note", "Swagger에서는 CORS로 인해 작동하지 않습니다. 브라우저에서 fullUrl을 직접 열어주세요."
            ));
        }

        return ResponseEntity.ok(java.util.Map.of(
                "providers", providerMap,
                "message", "현재 지원하는 소셜 로그인 제공자 목록입니다. OAuth2 로그인은 브라우저에서 직접 실행해주세요."
        ));
    }

    /**
     * 소셜 로그인 회원가입 완료
     * OAuth2 정보와 사용자가 입력한 핸드폰 번호를 결합하여 회원가입을 완료합니다.
     *
     * @param request 소셜 회원가입 요청 정보
     * @return 회원가입 완료 응답 (JWT 토큰 포함)
     */
    @PostMapping("/signup")
    @Operation(
            summary = "소셜 로그인 회원가입 완료",
            description = "OAuth2 정보와 핸드폰 번호를 결합하여 회원가입을 완료하고 JWT 토큰을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SocialSignupResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 필수 필드 누락, 유효하지 않은 이메일/전화번호 형식, 약관 미동의 등",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<SocialSignupResponse> completeSocialSignup(
            @Valid @RequestBody SocialSignupRequest request) {

        log.info("소셜 회원가입 완료 요청 - 제공자: {}, 이메일: {}, 핸드폰: {}",
                request.provider(), request.email(), request.getNormalizedPhoneNumber());

        // AuthService를 통해 소셜 회원가입 처리
        // BusinessException이 발생하면 GlobalExceptionHandler가 처리
        SocialSignupResponse response = authService.completeSocialSignup(request);

        log.info("소셜 회원가입 완료 성공 - 사용자 ID: {}, 이메일: {}",
                response.userId(), response.email());

        return ResponseEntity.ok(response);
    }
}