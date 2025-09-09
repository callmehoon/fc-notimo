package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.service.IndividualTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @PathVariable Integer workspaceId) {
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
            @PathVariable Integer workspaceId) {

        log.info("[ASYNC-ENTRY] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return individualTemplateService.createTemplateAsync(workspaceId).thenApply(ResponseEntity::ok);
    }

    /**
     * 개인 템플릿 전체 조회
     */
    @Operation(
            summary = "워크스페이스 별 개인 템플릿 목록 전체 조회",
            description = "페이지네이션 및 정렬(기본값 : 생성일자 기준) 지원"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "개별 템플릿 조회 성공")
    })
    @GetMapping("/{workspaceId}/templates")
    public ResponseEntity<Page<IndividualTemplateResponse>> getAllTemplates(
            @Parameter(description = "워크스페이스 ID", example = "1")
            @PathVariable Integer workspaceId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(individualTemplateService.getAllTemplates(workspaceId, pageable));
    }

    @Operation(summary = "워크스페이스 별 템플릿 목록 조회 (비동기 @Async)")
    @GetMapping("/{workspaceId}/templates/async")
    public CompletableFuture<ResponseEntity<Page<IndividualTemplateResponse>>> getAllTemplatesAsync(
            @Parameter(description = "워크스페이스 ID", example = "1")
            @PathVariable Integer workspaceId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return individualTemplateService.getAllTemplatesAsync(workspaceId, pageable).thenApply(ResponseEntity::ok);
    }

    /**
     * 개인 템플릿 단일 조회
     */
    @Operation(
            summary = "템플릿 단일 조회",
            description = "워크스페이스 별 개인 템플릿을 단일 조회"
    )
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

    @Operation(summary = "템플릿 단일 조회 (비동기 @Async)")
    @GetMapping("/{workspaceId}/templates/{individualTemplateId}/async")
    public CompletableFuture<ResponseEntity<IndividualTemplateResponse>> getTemplateAsync(
            @Parameter(description = "워크스페이스 ID", example = "1")
            @PathVariable Integer workspaceId,
            @Parameter(description = "개인 템플릿 ID", example = "2")
            @PathVariable Integer individualTemplateId) {
        return individualTemplateService.getIndividualTemplateAsync(workspaceId, individualTemplateId).thenApply(ResponseEntity::ok);
    }
}
