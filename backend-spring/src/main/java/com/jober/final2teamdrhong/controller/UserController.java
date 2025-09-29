package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.changePassword.PasswordResetRequest;
import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.dto.user.DeleteUserRequest;
import com.jober.final2teamdrhong.dto.user.UserProfileResponse;
import com.jober.final2teamdrhong.service.UserService;
import com.jober.final2teamdrhong.util.ClientIpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
@Tag(name = "사용자 관리", description = "사용자 정보 관리 API")
public class UserController {

    private final UserService userService;

    /**
     * 마이페이지에서 비밀번호 변경
     * 로그인한 사용자가 현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.
     * 변경 후 모든 세션이 무효화되어 재로그인이 필요합니다.
     *
     * @param request 비밀번호 변경 요청 정보
     * @param jwtClaims 현재 인증된 사용자 정보
     * @param httpRequest HTTP 요청 (클라이언트 IP 추출용)
     * @return 성공 응답
     */
    @Operation(
        summary = "비밀번호 변경",
        description = "마이페이지에서 현재 비밀번호 확인 후 새 비밀번호로 변경합니다. " +
                     "변경 후 모든 세션이 무효화되어 재로그인이 필요합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (현재 비밀번호 불일치, 유효성 검사 실패 등)"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)"),
        @ApiResponse(responseCode = "429", description = "요청 속도 제한 초과"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @Parameter(description = "비밀번호 변경 요청 정보", required = true)
            @Valid @RequestBody PasswordResetRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal JwtClaims jwtClaims,

            @Parameter(hidden = true)
            HttpServletRequest httpRequest) {

        log.info("비밀번호 변경 요청: userId={}", jwtClaims.getUserId());

        // 1. 사용자 ID 추출
        Integer userId = jwtClaims.getUserId();

        // 2. 클라이언트 IP 추출
        String clientIp = ClientIpUtil.getClientIpAddress(httpRequest);

        // 3. 비밀번호 변경 처리
        userService.changePassword(userId, request, clientIp);

        log.info("비밀번호 변경 완료: userId={}", userId);

        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다. 보안을 위해 모든 세션이 종료되었으므로 다시 로그인해주세요.");
    }

    /**
     * 회원 탈퇴 처리
     * 로그인한 사용자가 비밀번호 확인 후 회원 탈퇴를 진행합니다.
     * Soft Delete 방식으로 처리되며, 개인정보는 익명화됩니다.
     *
     * @param request 회원 탈퇴 요청 정보
     * @param jwtClaims 현재 인증된 사용자 정보
     * @param httpRequest HTTP 요청 (클라이언트 IP 추출용)
     * @return 성공 응답
     */
    @Operation(
        summary = "회원 탈퇴",
        description = "현재 비밀번호 확인 후 회원 탈퇴를 진행합니다. " +
                     "Soft Delete 방식으로 처리되며, 개인정보는 익명화됩니다. " +
                     "탈퇴 후 모든 세션이 무효화되고 계정 복구는 불가능합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (확인 문구 불일치, 이미 탈퇴한 계정 등)"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (비밀번호 불일치 또는 로그인 필요)"),
        @ApiResponse(responseCode = "429", description = "요청 속도 제한 초과"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount(
            @Parameter(description = "회원 탈퇴 요청 정보", required = true)
            @Valid @RequestBody DeleteUserRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal JwtClaims jwtClaims,

            @Parameter(hidden = true)
            HttpServletRequest httpRequest) {

        log.info("[ACCOUNT_DELETE] 회원 탈퇴 요청: userId={}", jwtClaims.getUserId());

        // 1. 사용자 ID 추출
        Integer userId = jwtClaims.getUserId();

        // 2. 클라이언트 IP 추출
        String clientIp = ClientIpUtil.getClientIpAddress(httpRequest);

        // 3. 회원 탈퇴 처리
        userService.deleteAccount(userId, request, clientIp);

        log.info("[ACCOUNT_DELETE] 회원 탈퇴 완료: userId={}", userId);

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.");
    }

    /**
     * 사용자 프로필 조회
     * 로그인한 사용자의 프로필 정보를 조회합니다. (읽기 전용)
     * 마이페이지에서 현재 계정 정보 확인용으로 사용됩니다.
     *
     * @param jwtClaims 현재 인증된 사용자 정보
     * @return 사용자 프로필 정보
     */
    @Operation(
        summary = "사용자 프로필 조회",
        description = "로그인한 사용자의 프로필 정보를 조회합니다. " +
                     "마이페이지에서 현재 계정 정보 확인용으로 사용되며 읽기 전용입니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal JwtClaims jwtClaims) {

        // 1. 인증 정보 확인
        if (jwtClaims == null) {
            return ResponseEntity.status(401).build();
        }

        log.info("사용자 프로필 조회 요청: userId={}", jwtClaims.getUserId());

        // 2. 사용자 ID 추출
        Integer userId = jwtClaims.getUserId();

        // 2. 프로필 정보 조회
        UserProfileResponse userProfile = userService.getUserProfile(userId);

        log.info("사용자 프로필 조회 완료: userId={}", userId);

        return ResponseEntity.ok(userProfile);
    }


}