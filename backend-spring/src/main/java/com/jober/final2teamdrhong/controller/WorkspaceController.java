package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.dto.workspace.WorkspaceRequest;
import com.jober.final2teamdrhong.dto.workspace.WorkspaceResponse;
import com.jober.final2teamdrhong.exception.ErrorResponse;
import com.jober.final2teamdrhong.service.WorkspaceService;
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
     * <p>
     * 요청 본문(RequestBody)으로 받은 데이터를 사용하여 워크스페이스를 생성하며, 이 API를 호출하기 위해서는 인증이 필요합니다.
     *
     * @param createDTO 클라이언트로부터 받은 워크스페이스 생성을 위한 데이터 전송 객체 (DTO) (JSON, @Valid로 검증됨)
     * @return 상태 코드 201 (Created)와 함께 생성된 워크스페이스의 간략 정보를 담은 ResponseEntity
     */
    @Operation(summary = "워크스페이스 생성", description = "새로운 워크스페이스를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "워크스페이스 생성 성공",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WorkspaceResponse.SimpleDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: 요청 데이터 유효성 검사에 실패했거나, 비즈니스 규칙에 위배됩니다. (예: 존재하지 않는 사용자, 중복된 URL)",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<WorkspaceResponse.SimpleDTO> createWorkspace(@Valid @RequestBody WorkspaceRequest.CreateDTO createDTO,
                                                                       @AuthenticationPrincipal JwtClaims jwtClaims) {
        Integer currentUserId = jwtClaims.getUserId();
        WorkspaceResponse.SimpleDTO createdWorkspace = workspaceService.createWorkspace(createDTO, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkspace);
    }
}
