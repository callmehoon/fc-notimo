package com.jober.final2teamdrhong.controller;


import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.dto.worksession.WorkSessionRequest;
import com.jober.final2teamdrhong.dto.worksession.WorkSessionResponse;
import com.jober.final2teamdrhong.exception.ErrorResponse;
import com.jober.final2teamdrhong.service.WorkSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/work-sessions")
@Tag(name = "작업 세션 API", description = "채팅방, 채팅내용, 템플릿이력 CRUD 관련 API")
public class WorkSessionController {

    private final WorkSessionService workSessionService;

    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "채팅 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkSessionResponse.InfoDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음 (워크스페이스가 존재하지 않거나, 사용자에게 권한이 없음)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<WorkSessionResponse.InfoDTO> createChatSession(
            @AuthenticationPrincipal JwtClaims jwtClaims,
            @Valid @RequestBody WorkSessionRequest.CreateDTO request) {
        Integer userId = jwtClaims.getUserId();
        WorkSessionResponse.InfoDTO response = workSessionService.createChatSession(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



    @Operation(summary = "특정 채팅방 조회", description = "특정 워크스페이스에 속한 특정 채팅방을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkSessionResponse.InfoDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음 (채팅방이 존재하지 않거나, 사용자에게 권한이 없음)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{workspaceId}/chat-sessions/{sessionId}")
    public ResponseEntity<WorkSessionResponse.InfoDTO> getChatSession(
            @AuthenticationPrincipal JwtClaims jwtClaims,
            @PathVariable("workspaceId") Integer workspaceId,
            @PathVariable("sessionId") Integer sessionId) {
        Integer userId = jwtClaims.getUserId();
        WorkSessionResponse.InfoDTO response = workSessionService.getChatSession(sessionId, workspaceId, userId);
        return ResponseEntity.ok(response);
    }
}

