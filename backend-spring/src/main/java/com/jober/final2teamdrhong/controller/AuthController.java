package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.config.JwtConfig;
import com.jober.final2teamdrhong.dto.auth.AddLocalAuthRequest;
import com.jober.final2teamdrhong.dto.changePassword.ConfirmPasswordResetRequest;
import com.jober.final2teamdrhong.dto.emailVerification.EmailRequest;
import com.jober.final2teamdrhong.dto.emailVerification.EmailVerificationResponse;
import com.jober.final2teamdrhong.dto.userLogin.TokenRefreshResponse;
import com.jober.final2teamdrhong.dto.userLogin.UserLoginRequest;
import com.jober.final2teamdrhong.dto.userLogin.UserLoginResponse;
import com.jober.final2teamdrhong.dto.userLogout.UserLogoutRequest;
import com.jober.final2teamdrhong.dto.userSignup.UserSignupRequest;
import com.jober.final2teamdrhong.exception.ErrorResponse;
import com.jober.final2teamdrhong.service.AuthService;
import com.jober.final2teamdrhong.service.EmailService;
import com.jober.final2teamdrhong.service.RateLimitService;
import com.jober.final2teamdrhong.service.UserService;
import com.jober.final2teamdrhong.util.ClientIpUtil;
import com.jober.final2teamdrhong.util.LogMaskingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.jober.final2teamdrhong.service.AuthService.AuthMethodsResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
@Tag(name = "인증", description = "사용자 인증 관련 API (회원가입, 로그인, 이메일 인증)")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final RateLimitService rateLimitService;
    private final UserService userService;
    private final JwtConfig jwtConfig;

    @Value("${app.environment.development:true}")
    private boolean isDevelopment;

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입을 위한 6자리 인증 코드를 이메일로 발송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 코드 발송 성공",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EmailVerificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: 이메일 형식 오류 등",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Rate Limit 초과",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/send-verification-code")
    public ResponseEntity<EmailVerificationResponse> sendVerificationCode(
            @Parameter(description = "인증 코드를 받을 이메일 주소", required = true)
            @Valid @RequestBody EmailRequest emailRequest,
            HttpServletRequest request) {

        String clientIp = ClientIpUtil.getClientIpAddress(request, isDevelopment);

        // Rate limiting 로직을 서비스로 위임
        emailService.sendVerificationCodeWithRateLimit(emailRequest.email(), clientIp);

        log.info("인증 코드 발송 성공: ip={}, email={}", clientIp, emailRequest.email());
        return ResponseEntity.ok(
            EmailVerificationResponse.success("인증 코드가 발송되었습니다.")
        );
    }

    @Operation(summary = "로컬 회원가입", description = "이메일 인증을 완료한 후 로컬 계정으로 회원가입을 진행합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "회원가입 성공",
            content = @Content(
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "회원가입 성공",
                        value = "{}"


                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (중복 이메일, 잘못된 인증코드, 유효성 검증 실패 등)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate Limit 초과",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Parameter(description = "회원가입 요청 정보 (사용자명, 이메일, 비밀번호, 인증코드 포함)", required = true)
            @Valid @RequestBody UserSignupRequest userSignupRequest,
            HttpServletRequest request) {

        String clientIp = ClientIpUtil.getClientIpAddress(request, isDevelopment);

        // Rate limiting과 회원가입 로직을 서비스로 위임
        authService.signupWithRateLimit(userSignupRequest, clientIp);

        log.info("회원가입 성공: ip={}, email={}", clientIp, userSignupRequest.email());
        return ResponseEntity.status(201).build();


    }

    @Operation(summary = "로컬 로그인", description = "이메일과 비밀번호를 사용하여 로컬 계정으로 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: 인증 실패 등",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Rate Limit 초과",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/login", produces = "application/json;charset=UTF-8")
    public ResponseEntity<UserLoginResponse> login(
            @Parameter(description = "로그인 요청 정보 (이메일, 비밀번호)", required = true)
            @RequestBody @Valid UserLoginRequest userLoginRequest,
            HttpServletRequest request) {

        String clientIp = ClientIpUtil.getClientIpAddress(request, isDevelopment);

        // 향상된 Rate limiting 체크 (IP + 이메일 기반)
        rateLimitService.checkEnhancedLoginRateLimit(clientIp, userLoginRequest.email());

        UserLoginResponse response = authService.loginWithRefreshToken(userLoginRequest, clientIp);

        // 보안 강화: 민감한 정보 마스킹 후 로깅
        log.info("로그인 API 완료: ip={}, email={}",
                LogMaskingUtil.maskIpAddress(clientIp),
                LogMaskingUtil.maskEmail(userLoginRequest.email()));

        // 보안 개선: Authorization 헤더에는 토큰을 포함하지 않고, 응답 바디에만 포함
        // 클라이언트는 응답 바디에서 토큰을 추출하여 이후 요청의 헤더에 포함해야 함
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "토큰 갱신", description = "만료된 Access Token을 Refresh Token으로 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TokenRefreshResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Rate Limit 초과",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Parameter(description = "Authorization 헤더 (Bearer + Refresh Token)", required = true)
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletRequest request) {

        String clientIp = ClientIpUtil.getClientIpAddress(request, isDevelopment);

        // Rate limiting 체크 (토큰 갱신 남용 방지)
        rateLimitService.checkRefreshTokenRateLimit(clientIp);

        // AuthService에 토큰 갱신 처리 위임
        TokenRefreshResponse response = authService.refreshTokens(authorizationHeader, clientIp);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 종료하고 모든 토큰을 무효화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Authorization 헤더 (Bearer + Access Token)")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "로그아웃 요청 (Refresh Token 포함)")
            @Valid @RequestBody UserLogoutRequest logoutRequest,
            HttpServletRequest request) {

        String clientIp = ClientIpUtil.getClientIpAddress(request, isDevelopment);

        // AuthService에 로그아웃 처리 위임
        String accessToken = jwtConfig.extractTokenFromHeader(authorizationHeader);
        authService.logout(accessToken, logoutRequest.refreshToken(), clientIp);

        return ResponseEntity.noContent().build();
    }

    /**
     * 비밀번호 재설정 (이메일 인증 후)
     * 비밀번호를 잊어버린 사용자가 이메일로 받은 인증 코드를 통해 비밀번호를 재설정합니다.
     * 재설정 후 모든 세션이 무효화되어 재로그인이 필요합니다.
     *
     * @param request 비밀번호 재설정 요청 정보 (이메일, 인증코드, 새 비밀번호)
     * @param httpRequest HTTP 요청 (클라이언트 IP 추출용)
     * @return 성공 응답
     */
    @Operation(
        summary = "비밀번호 재설정",
        description = "이메일 인증 코드를 통해 비밀번호를 재설정합니다. " +
                     "재설정 후 모든 세션이 무효화되어 재로그인이 필요합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (인증 코드 불일치/만료, 사용자 없음, 유효성 검사 실패 등)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate Limit 초과",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Parameter(description = "비밀번호 재설정 요청 정보", required = true)
            @Valid @RequestBody ConfirmPasswordResetRequest request,

            @Parameter(hidden = true)
            HttpServletRequest httpRequest) {

        log.info("비밀번호 재설정 요청: email={}", LogMaskingUtil.maskEmail(request.email()));

        // 1. 클라이언트 IP 추출
        String clientIp = ClientIpUtil.getClientIpAddress(httpRequest, isDevelopment);

        // 2. UserService에 비밀번호 재설정 처리 위임
        userService.resetPassword(request, clientIp);

        log.info("비밀번호 재설정 완료: email={}", LogMaskingUtil.maskEmail(request.email()));

        return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다. 보안을 위해 모든 세션이 종료되었으므로 새 비밀번호로 로그인해주세요.");
    }

    // ====================================
    // 계정 통합 관련 API 엔드포인트들
    // ====================================

    /**
     * 소셜 로그인 사용자가 로컬 인증을 추가 (계정 통합)
     * 마이페이지에서 소셜 로그인 사용자가 이메일 인증 후 비밀번호를 설정하여
     * 소셜 로그인과 로컬 로그인을 모두 사용할 수 있도록 합니다.
     *
     * @param request 로컬 인증 추가 요청 정보 (이메일, 인증코드, 비밀번호)
     * @param userDetails 현재 인증된 사용자 정보
     * @param httpRequest HTTP 요청 (클라이언트 IP 추출용)
     * @return 성공 응답
     */
    @Operation(
        summary = "계정 통합 - 소셜 계정에 로컬 인증 추가",
        description = "소셜 로그인 사용자가 이메일 인증 후 비밀번호를 설정하여 " +
                     "소셜 로그인과 로컬 로그인을 모두 사용할 수 있도록 계정을 통합합니다. " +
                     "통합 후 모든 기존 토큰이 무효화되어 재로그인이 필요합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "계정 통합 성공"),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (인증 코드 불일치/만료, 이메일 불일치, 이미 로컬 인증 존재 등)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (로그인 필요)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate Limit 초과",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/add-local-auth")
    public ResponseEntity<String> addLocalAuth(
            @Parameter(description = "로컬 인증 추가 요청 정보", required = true)
            @Valid @RequestBody AddLocalAuthRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,

            @Parameter(hidden = true)
            HttpServletRequest httpRequest) {

        log.info("계정 통합 요청 - 소셜→로컬: user={}", userDetails.getUsername());

        // 1. 사용자 ID 추출 (UserDetails에서 username이 userId로 설정됨)
        Integer userId = Integer.valueOf(userDetails.getUsername());

        // 2. 클라이언트 IP 추출
        String clientIp = ClientIpUtil.getClientIpAddress(httpRequest, isDevelopment);

        // 3. AuthService에 계정 통합 처리 위임
        authService.addLocalAuth(userId, request, clientIp);

        log.info("계정 통합 완료 - 소셜→로컬: userId={}", userId);

        return ResponseEntity.ok("계정 통합이 완료되었습니다. 이제 소셜 로그인과 로컬 로그인을 모두 사용할 수 있습니다. " +
                               "보안을 위해 모든 세션이 종료되었으므로 다시 로그인해주세요.");
    }

    /**
     * 사용자의 연결된 인증 방법 조회
     * 현재 사용자가 어떤 인증 방법들(로컬, 구글 등)을 연결했는지 조회합니다.
     * 마이페이지에서 계정 통합 상태를 확인할 때 사용됩니다.
     *
     * @param userDetails 현재 인증된 사용자 정보
     * @return 연결된 인증 방법 목록
     */
    @Operation(
        summary = "연결된 인증 방법 조회",
        description = "현재 사용자가 연결한 인증 방법들(로컬, 구글 등)을 조회합니다. " +
                     "마이페이지에서 계정 통합 상태를 확인할 때 사용됩니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = AuthMethodsResponse.class,
                    example = """
                    {
                        "hasLocalAuth": true,
                        "socialMethods": ["GOOGLE", "KAKAO"]
                    }
                    """,
                    description = "연결된 인증 방법 정보 (로컬 인증 보유 여부 + 소셜 인증 목록)"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (로그인 필요)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/connected-methods")
    public ResponseEntity<AuthMethodsResponse> getConnectedAuthMethods(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("연결된 인증 방법 조회: user={}", userDetails.getUsername());

        // 1. 사용자 ID 추출
        Integer userId = Integer.valueOf(userDetails.getUsername());

        // 2. AuthService에 조회 처리 위임 - 기존에 추상화된 응답 객체 활용
        AuthMethodsResponse response = authService.getConnectedAuthMethods(userId);

        log.info("연결된 인증 방법 조회 완료: userId={}, hasLocal={}, socialMethods={}",
                userId, response.hasLocalAuth(), response.socialMethods());

        return ResponseEntity.ok(response);
    }
}