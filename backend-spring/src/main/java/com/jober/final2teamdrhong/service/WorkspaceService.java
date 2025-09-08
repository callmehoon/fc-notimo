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

import java.util.List;

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

    /**
     * 특정 사용자가 속한 모든 워크스페이스의 목록을 조회합니다.
     * <p>
     * userId를 통해 다른 사용자의 워크스페이스가 조회되지 않도록 인가(Authorization) 처리를 수행합니다.
     *
     * @param userId 조회할 사용자의 ID (현재 인증된 사용자의 ID)
     * @return 해당 사용자의 워크스페이스 간략 정보(SimpleDTO) 리스트
     * @throws IllegalArgumentException 해당 ID의 사용자가 존재하지 않을 경우 발생
     */
    public List<WorkspaceResponse.SimpleDTO> readWorkspaces(Integer userId) {
        // User 조회 및 예외 처리 로직, create와 다르게 존재하는지 확인만 하면 되므로, 'User user =' 는 불필요
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        List<Workspace> workspaceList = workspaceRepository.findAllByUser_UserId(userId);

        return workspaceList.stream()
                .map(WorkspaceResponse.SimpleDTO::new)
                .toList();
    }

    /**
     * 특정 워크스페이스의 상세 정보를 조회합니다.
     * <p>
     * 요청한 사용자가 해당 워크스페이스의 소유자인지 확인하는 인가 과정이 포함됩니다.
     *
     * @param workspaceId 조회할 워크스페이스의 ID
     * @param userId      조회할 사용자의 ID (현재 인증된 사용자의 ID)
     * @return 워크스페이스의 상세 정보(DetailDTO)
     * @throws IllegalArgumentException 해당 워크스페이스가 존재하지 않거나, 사용자가 소유자가 아닐 경우 발생
     */
    public WorkspaceResponse.DetailDTO readWorkspaceDetail(Integer workspaceId, Integer userId) {
        // DB 조회를 한 번으로 최적화하여 워크스페이스 존재 여부와 소유권을 동시에 확인
        // 예외 메세지에, userId는 안넣는 이유:
        // 워크스페이스가 원래부터 없어서 에러가 난 건지 유저에게 권한이 없어서 에러가 난 건지
        // 공격자 입장에서는 이 두 가지 상황을 전혀 구분할 수 없기 때문에 보안상 더 안전함
        Workspace workspaceDetail = workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + workspaceId));

        // Optional에서 꺼낸 Workspace 엔티티를 DTO 생성자에 전달
        return new WorkspaceResponse.DetailDTO(workspaceDetail);
    }
}
