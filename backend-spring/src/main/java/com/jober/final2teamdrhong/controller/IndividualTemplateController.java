package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.service.IndividualTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "개인 템플릿 API", description = "개인 템플릿 생성/조회/수정/삭제 관련 API")

public class IndividualTemplateController {

    private final IndividualTemplateService individualTemplateService;

    /**
     * 빈 템플릿 생성 API
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
        IndividualTemplateResponse response = individualTemplateService.createTemplate(workspaceId);
        return ResponseEntity.ok(response);
    }
}
