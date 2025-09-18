package com.jober.final2teamdrhong.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jober.final2teamdrhong.service.PublicTemplateService;
import com.jober.final2teamdrhong.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;


/**
 * 관리자용 컨트롤러.
 * ADMIN 권한을 가진 사용자만 접근 가능하며, 현재는 공용 템플릿 삭제 기능을 제공한다.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "관리자 API", description = "관리자 전용 리소스 관리 및 운영 기능을 제공합니다")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final PublicTemplateService publicTemplateService;

    /**
     * ADMIN 권한을 가진 사용자만 공용 템플릿을 삭제할 수 있습니다.
     *
     * @param publicTemplateId 삭제할 공용 템플릿 ID
     * @return 204 No Content (본문 없음)
     * @throws IllegalArgumentException 요청한 공용 템플릿이 존재하지 않을 경우
     */
    @Operation(
            summary = "공용 템플릿 삭제",
            description = "ADMIN 권한을 가진 사용자만 공용 템플릿을 삭제할 수 있습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "공용 템플릿 삭제 성공 (응답 본문 없음)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 존재하지 않는 템플릿 ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 유효하지 않은 JWT 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 부족 - ADMIN 권한이 없음",
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
    @DeleteMapping("/public-templates/{publicTemplateId}")
    public ResponseEntity<Void> deletePublicTemplate(@PathVariable Integer publicTemplateId) {
        publicTemplateService.deletePublicTemplate(publicTemplateId);
        return ResponseEntity.noContent().build();
    }
}