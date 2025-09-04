package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.workspace.WorkspaceRequest;
import com.jober.final2teamdrhong.dto.workspace.WorkspaceResponse;
import com.jober.final2teamdrhong.service.WorkspaceService;
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
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    /**
     * 새로운 워크스페이스를 생성하는 API
     * 요청 본문에 담긴 워크스페이스 정보의 유효성을 검사한 후 서비스를 호출합니다.
     *
     * @param createDTO 클라이언트로부터 받은 워크스페이스 생성 정보 (JSON, @Valid로 검증됨)
     * @return 생성된 워크스페이스의 간략한 정보와 HTTP 상태 코드 201 (Created)
     * 
     */
    @PostMapping("/workspaces")
    public ResponseEntity<WorkspaceResponse.SimpleDTO> createWorkspace(@Valid @RequestBody WorkspaceRequest.CreateDTO createDTO) {
        // TODO: Spring Security 도입 후, @AuthenticationPrincipal 등을 통해 실제 사용자 정보 획득 필요
        Integer currentUserId = 1;
        WorkspaceResponse.SimpleDTO createdWorkspace = workspaceService.createWorkspace(createDTO, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkspace);
    }
}
