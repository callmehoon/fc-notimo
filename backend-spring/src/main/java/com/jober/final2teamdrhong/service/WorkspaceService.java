package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.workspace.WorkspaceRequest;
import com.jober.final2teamdrhong.dto.workspace.WorkspaceResponse;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 워크스페이스 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    /**
     * 인증된 사용자를 위해 새로운 워크스페이스를 생성합니다.
     * <p>
     * userId 매개변수를 통해 워크스페이스의 소유자를 지정하며, 이는 생성 권한에 대한 인가(Authorization) 역할을 합니다.
     *
     * @param createDTO 워크스페이스 생성을 위한 요청 데이터
     * @param userId    워크스페이스를 생성할 사용자의 ID (현재 인증된 사용자)
     * @return 생성된 워크스페이스의 간략 정보(SimpleDTO)
     * @throws IllegalArgumentException 해당 ID의 사용자가 존재하지 않거나, 요청된 URL이 이미 사용 중일 경우 발생
     */
    @Transactional
    public WorkspaceResponse.SimpleDTO createWorkspace(WorkspaceRequest.CreateDTO createDTO, Integer userId) {
        // User 조회 및 예외 처리 로직
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        // workspaceUrl unique 조건 확인 로직
        if (workspaceRepository.existsByWorkspaceUrl(createDTO.getWorkspaceUrl())) {
            throw new IllegalArgumentException("이미 사용 중인 URL입니다. 다른 URL을 입력해주세요.");
        }

        Workspace workspace = Workspace.builder()
                .workspaceName(createDTO.getWorkspaceName())
                .workspaceSubname(createDTO.getWorkspaceSubname())
                .workspaceAddress(createDTO.getWorkspaceAddress())
                .workspaceDetailAddress(createDTO.getWorkspaceDetailAddress())
                .workspaceUrl(createDTO.getWorkspaceUrl())
                .representerName(createDTO.getRepresenterName())
                .representerPhoneNumber(createDTO.getRepresenterPhoneNumber())
                .representerEmail(createDTO.getRepresenterEmail())
                .companyName(createDTO.getCompanyName())
                .companyRegisterNumber(createDTO.getCompanyRegisterNumber())
                .build();

        workspace.setUser(user);
        Workspace savedWorkspace = workspaceRepository.save(workspace);

        return new WorkspaceResponse.SimpleDTO(savedWorkspace);
    }
}
