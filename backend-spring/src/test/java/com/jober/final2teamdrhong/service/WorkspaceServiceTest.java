package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.workspace.WorkspaceRequest;
import com.jober.final2teamdrhong.dto.workspace.WorkspaceResponse;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Test
    @DisplayName("워크스페이스 생성 성공 테스트")
    void createWorkspace_Success() {
        // given (테스트 준비)
        Integer userId = 1;
        WorkspaceRequest.CreateDTO createDTO = WorkspaceRequest.CreateDTO.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("홍길동")
                .representerPhoneNumber("010-1234-5678")
                .companyName("테스트 회사")
                .build();

        // Mockito를 사용하여 User와 Workspace의 가짜(Mock) 객체 생성
        User mockUser = mock(User.class);
        Workspace mockWorkspace = mock(Workspace.class);

        // Mockito 행동 정의 (Stubbing)
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(workspaceRepository.existsByWorkspaceUrl(createDTO.getWorkspaceUrl())).thenReturn(false);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(mockWorkspace);

        // DTO 생성을 위해 가짜 객체의 getter가 특정 값을 반환하도록 설정
        // SimpleDTO 생성자에서 호출하는 모든 getter를 명시적으로 정의해줍니다.
        when(mockWorkspace.getWorkspaceId()).thenReturn(1);
        when(mockWorkspace.getWorkspaceName()).thenReturn(createDTO.getWorkspaceName());
        when(mockWorkspace.getWorkspaceSubname()).thenReturn(createDTO.getWorkspaceSubname());
        when(mockWorkspace.getCreatedAt()).thenReturn(LocalDateTime.now());

        // when (테스트 실행)
        WorkspaceResponse.SimpleDTO result = workspaceService.createWorkspace(createDTO, userId);

        // then (결과 검증)
        assertNotNull(result);
        assertEquals(1, result.getWorkspaceId());
        assertEquals("테스트 워크스페이스", result.getWorkspaceName());

        // workspaceRepository.save가 한 번 호출되었는지 검증
        verify(workspaceRepository, times(1)).save(any(Workspace.class));
    }

    @Test
    @DisplayName("워크스페이스 생성 실패 테스트 - 존재하지 않는 사용자")
    void createWorkspace_Fail_UserNotFound() {
        // given
        Integer userId = 999; // 존재하지 않는 사용자 ID
        WorkspaceRequest.CreateDTO createDTO = WorkspaceRequest.CreateDTO.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("홍길동")
                .representerPhoneNumber("010-1234-5678")
                .companyName("테스트 회사")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.createWorkspace(createDTO, userId));

        assertEquals("해당 사용자를 찾을 수 없습니다. ID: " + userId, exception.getMessage());
        verify(workspaceRepository, never()).save(any(Workspace.class)); // save는 절대 호출되면 안 됨
    }

    @Test
    @DisplayName("워크스페이스 생성 실패 테스트 - 중복된 URL")
    void createWorkspace_Fail_DuplicateUrl() {
        // given
        Integer userId = 1;
        WorkspaceRequest.CreateDTO createDTO = WorkspaceRequest.CreateDTO.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("duplicate-url")
                .representerName("홍길동")
                .representerPhoneNumber("010-1234-5678")
                .companyName("테스트 회사")
                .build();

        User mockUser = mock(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(workspaceRepository.existsByWorkspaceUrl(createDTO.getWorkspaceUrl())).thenReturn(true); // URL이 이미 존재한다고 설정

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.createWorkspace(createDTO, userId));

        assertEquals("이미 사용 중인 URL입니다. 다른 URL을 입력해주세요.", exception.getMessage());
        verify(workspaceRepository, never()).save(any(Workspace.class)); // save는 절대 호출되면 안 됨
    }
}