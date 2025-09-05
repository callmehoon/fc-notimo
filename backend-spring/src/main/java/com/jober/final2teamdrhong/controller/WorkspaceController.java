package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.workspace.WorkspaceRequest;
import com.jober.final2teamdrhong.dto.workspace.WorkspaceResponse;
import com.jober.final2teamdrhong.exception.ErrorResponse;
import com.jober.final2teamdrhong.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 워크스페이스 관련 HTTP 요청을 처리하는 컨트롤러입니다.
 */
@Tag(name = "워크스페이스 관리 API", description = "워크스페이스 생성, 조회, 수정, 삭제 등 워크스페이스와 관련된 API를 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    /**
     * 새로운 워크스페이스를 생성하는 API
     * 요청 본문에 담긴 워크스페이스 정보의 유효성을 검사한 후 서비스를 호출합니다.
     *
     * @param createDTO 클라이언트로부터 받은 워크스페이스 생성 정보 (JSON, @Valid로 검증됨)
     * @return 생성된 워크스페이스의 간략한 정보와 HTTP 상태 코드 201 (Created)
     */
    @Operation(summary = "워크스페이스 생성", description = "새로운 워크스페이스를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "워크스페이스 생성 성공",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WorkspaceResponse.SimpleDTO.class))),
            @ApiResponse(responseCode = "400", description = "요청 데이터 유효성 검사 실패 (예: 필수 필드 누락)",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 URL일 경우 (Conflict)",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<WorkspaceResponse.SimpleDTO> createWorkspace(@Valid @RequestBody WorkspaceRequest.CreateDTO createDTO) {
        // TODO: Spring Security 도입 후, @AuthenticationPrincipal 등을 통해 실제 사용자 정보 획득 필요
        Integer currentUserId = 1;
        WorkspaceResponse.SimpleDTO createdWorkspace = workspaceService.createWorkspace(createDTO, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkspace);
    }
}
