package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.service.IndividualTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "개인 템플릿 API", description = "개인 템플릿 생성/조회/수정/삭제 관련 API")

public class IndividualTemplateController {

    private final IndividualTemplateService individualTemplateService;

    /**
     * 동기 빈 템플릿 생성 API
     * body: {"workspaceId" : 123}
     */
    @PostMapping("/templates/{workspaceId}")
    @Operation(
            summary = "빈 템플릿 생성",
            description = "Workspace ID를 기반으로 빈 템플릿을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "템플릿 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<IndividualTemplateResponse> createEmptyTemplate(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Integer workspaceId,
            @AuthenticationPrincipal JwtClaims claims
    ) {
        Integer userId = claims.getUserId();
        individualTemplateService.validateWorkspaceOwnership(workspaceId, userId);

        log.info("[SYNC] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        IndividualTemplateResponse response = individualTemplateService.createTemplate(workspaceId);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 비동기 템플릿 생성 API (@Async)
     */
    @PostMapping("/templates/{workspaceId}/async")
    @Operation(
            summary = "빈 템플릿 생성(비동기 @Async)",
            description = "Workspace ID를 기반으로 빈 템플릿을 비동기로 생성합니다. (가상 스레드 확인용)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "템플릿 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public CompletableFuture<ResponseEntity<IndividualTemplateResponse>> createEmptyTemplateAsync(
            @Parameter(description = "Workspace ID", example = "1")
            @PathVariable Integer workspaceId,
            @AuthenticationPrincipal JwtClaims claims) {
        Integer userId = claims.getUserId();
        individualTemplateService.validateWorkspaceOwnership(workspaceId, userId);

        log.info("[ASYNC-ENTRY] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return individualTemplateService.createTemplateAsync(workspaceId).thenApply(ResponseEntity::ok);
    }

    // 전체 조회 (동기)
    @Operation(summary = "워크스페이스 별 개인 템플릿 목록 전체 조회", description = "페이지네이션 및 정렬 조건 지원")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "개별 템플릿 조회 성공") })
    @GetMapping("/{workspaceId}/templates")
    public ResponseEntity<Page<IndividualTemplateResponse>> getAllTemplates(
            @Parameter(description = "워크스페이스 ID", example = "1")
            @PathVariable Integer workspaceId,
            @RequestParam(defaultValue = "latest") String sortType, // latest | title
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(individualTemplateService.getAllTemplates(workspaceId, sortType, pageable));
    }

    // 전체 조회 (비동기)
    @Operation(summary = "워크스페이스 별 템플릿 목록 전체 조회 (비동기 @Async)")
    @GetMapping("/{workspaceId}/templates/async")
    public CompletableFuture<ResponseEntity<Page<IndividualTemplateResponse>>> getAllTemplatesAsync(
            @Parameter(description = "워크스페이스 ID", example = "1")
            @PathVariable Integer workspaceId,
            @RequestParam(defaultValue = "latest") String sortType,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable) {
        return individualTemplateService.getAllTemplatesAsync(workspaceId, sortType, pageable)
                .thenApply(ResponseEntity::ok);
    }

    // 상태별 조회 (동기)
    @Operation(summary = "워크스페이스 별 개인 템플릿 상태별 조회",
            description = "status 값(DRAFT, APPROVED 등)에 따른 템플릿 목록 조회. 페이지네이션 및 정렬 지원")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태별 템플릿 조회"),
            @ApiResponse(responseCode = "400", description = "잘못된 status 값"),
            @ApiResponse(responseCode = "404", description = "해당 조건에 맞는 템플릿 없음")
    })
    @GetMapping("/{workspaceId}/templates/status/{status}")
    public ResponseEntity<Page<IndividualTemplateResponse>> getTemplatesByStatus(
            @Parameter(description = "워크스페이스 ID", example = "1")
            @PathVariable Integer workspaceId,
            @Parameter(description = "조회할 템플릿 상태", example = "DRAFT")
            @PathVariable IndividualTemplate.Status status,
            @Parameter(description = "정렬 타입: latest | title", example = "latest")
            @RequestParam(defaultValue = "latest") String sortType,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                individualTemplateService.getIndividualTemplateByStatus(workspaceId, status, sortType, pageable)
        );
    }

    // 상태별 조회 (비동기)
    @Operation(summary = "워크스페이스 별 개인 템플릿 상태별 조회 (비동기 @Async)")
    @GetMapping("/{workspaceId}/templates/status/{status}/async")
    public CompletableFuture<ResponseEntity<Page<IndividualTemplateResponse>>> getTemplatesByStatusAsync(
            @Parameter(description = "워크스페이스 ID", example = "1")
            @PathVariable Integer workspaceId,
            @Parameter(description = "조회할 템플릿 상태", example = "APPROVED")
            @PathVariable("status") IndividualTemplate.Status status,
            @Parameter(description = "정렬 타입: latest | title", example = "title")
            @RequestParam(defaultValue = "latest") String sortType,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable) {
        return individualTemplateService.getIndividualTemplateByStatusAsync(workspaceId, status, sortType, pageable)
                .thenApply(ResponseEntity::ok);
    }

    // 단일 조회 (동기)
    @Operation(summary = "템플릿 단일 조회", description = "워크스페이스 별 개인 템플릿을 단일 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "템플릿 없음")
    })
    @GetMapping("/{workspaceId}/templates/{individualTemplateId}")
    public ResponseEntity<IndividualTemplateResponse> getTemplate(
            @Parameter(description = "워크스페이스 ID", example = "1")
            @PathVariable Integer workspaceId,
            @Parameter(description = "개인 템플릿 ID", example = "2")
            @PathVariable Integer individualTemplateId) {
        return ResponseEntity.ok(individualTemplateService.getIndividualTemplate(workspaceId, individualTemplateId));
    }

    // 단일 조회 (비동기)
    @Operation(summary = "템플릿 단일 조회 (비동기 @Async)")
    @GetMapping("/{workspaceId}/templates/{individualTemplateId}/async")
    public CompletableFuture<ResponseEntity<IndividualTemplateResponse>> getTemplateAsync(
            @Parameter(description = "워크스페이스 ID", example = "1")
            @PathVariable Integer workspaceId,
            @Parameter(description = "개인 템플릿 ID", example = "2")
            @PathVariable Integer individualTemplateId) {
        return individualTemplateService.getIndividualTemplateAsync(workspaceId, individualTemplateId)
                .thenApply(ResponseEntity::ok);
    }
}
