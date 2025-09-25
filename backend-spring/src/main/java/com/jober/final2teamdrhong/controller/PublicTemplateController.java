package com.jober.final2teamdrhong.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateCreateRequest;
import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplatePageableRequest;
import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateResponse;
import com.jober.final2teamdrhong.exception.ErrorResponse;
import com.jober.final2teamdrhong.service.PublicTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public-templates")
@Tag(name = "공용템플릿 관리 API", description = "공용 템플릿 생성, 조회 기능을 제공합니다")
public class PublicTemplateController {
    private final PublicTemplateService publicTemplateService;


    /**
     * 공용 템플릿 목록을 조회합니다.
     * 삭제되지 않은 템플릿만 조회되며, 다양한 정렬 옵션과 페이징을 지원합니다.
     *
     * @param request 페이징/정렬 요청 DTO
     *                - 기본값: page=0, size=10, sort=createdAt, direction=DESC
     *                - 요청 파라미터: page(페이지번호), size(페이지크기), sort(정렬필드), direction(정렬방향)
     *                - 허용 정렬필드: createdAt, shareCount, viewCount, publicTemplateTitle
     *                - 허용 정렬방향: ASC, DESC
     * @return 페이징된 공용 템플릿 응답 객체
     */
    @Operation(
        summary = "공용 템플릿 목록 조회",
        description = "삭제되지 않은 공용 템플릿 목록을 페이징하여 조회합니다. " +
                     "생성일시, 공유수, 조회수, 제목으로 정렬 가능하며 기본값은 생성일시 내림차순입니다. " +
                     "쿼리 파라미터: page(페이지번호, 기본값:0), size(페이지크기, 기본값:10), " +
                     "sort(정렬필드, 기본값:createdAt), direction(정렬방향, 기본값:DESC)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "공용 템플릿 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object",
                    description = "페이징된 공용 템플릿 목록",
                    example = "{\"content\":[{\"publicTemplateId\":1,\"publicTemplateTitle\":\"회의 안내 템플릿\",\"publicTemplateContent\":\"안녕하세요. 회의 일정을 안내드립니다...\",\"buttonTitle\":\"회의 참석하기\"}],\"pageable\":{\"sort\":{\"sorted\":true,\"unsorted\":false},\"pageNumber\":0,\"pageSize\":10},\"totalElements\":1,\"totalPages\":1,\"size\":10,\"number\":0,\"numberOfElements\":1,\"first\":true,\"last\":true,\"empty\":false}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터 - 유효하지 않은 페이징/정렬 파라미터",
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
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping
    public Page<PublicTemplateResponse> getPublicTemplates(
        @ParameterObject @Valid PublicTemplatePageableRequest request
    ) {
        Pageable pageable = request.toPageable();
        return publicTemplateService.getTemplates(pageable);
    }

    /**
     * 개인 템플릿을 기반으로 새로운 공용 템플릿을 생성합니다.
     * 생성된 공용 템플릿은 수정할 수 없으며, 요청 시점의 개인 템플릿 데이터를 그대로 복사합니다.
     *
     * @param request 공용 템플릿 생성 요청 DTO (individualTemplateId 필수)
     * @return 생성된 공용 템플릿 정보 {@link PublicTemplateResponse}
     * @throws IllegalArgumentException 요청한 개인 템플릿이 존재하지 않을 경우
     */
    @Operation(
        summary = "공용 템플릿 생성",
        description = "사용자의 개인 템플릿 ID를 기반으로 새로운 공용 템플릿을 생성합니다. " +
                    "생성된 템플릿은 수정 불가하며, 생성 시점의 title, content, buttonTitle을 그대로 복사합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "공용 템플릿 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PublicTemplateResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 - 존재하지 않는 개인 템플릿 ID, 또는 해당 개인 템플릿의 워크스페이스가 현재 사용자의 소유가 아닐 경우",
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
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping
    public ResponseEntity<PublicTemplateResponse> createPublicTemplate(
        @Valid @RequestBody PublicTemplateCreateRequest request,
        @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        Integer currentUserId = jwtClaims.getUserId();
        PublicTemplateResponse response = publicTemplateService.createPublicTemplate(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
