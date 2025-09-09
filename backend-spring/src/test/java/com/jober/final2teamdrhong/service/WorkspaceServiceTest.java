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
        // given
        Integer userId = 1;
        // 1. 워크스페이스 생성 요청 DTO 생성
        WorkspaceRequest.CreateDTO createDTO = WorkspaceRequest.CreateDTO.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("홍길동")
                .representerPhoneNumber("010-1234-5678")
                .companyName("테스트 회사")
                .build();

        // 2. Mockito를 사용하여 User와 Workspace의 가짜(Mock) 객체 생성
        User mockUser = mock(User.class);
        Workspace mockWorkspace = mock(Workspace.class);

        // 3. Mock 동작 정의 - 메소드 호출 시 반환값 설정
        //    3-1. 사용자 조회 성공
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        //    3-2. URL 중복 없음
        when(workspaceRepository.existsByWorkspaceUrl(createDTO.getWorkspaceUrl())).thenReturn(false);
        //    3-3. 저장 성공
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(mockWorkspace);

        // 4. 응답 DTO 생성을 위한 Mock 객체 getter 설정
        when(mockWorkspace.getWorkspaceId()).thenReturn(1);
        when(mockWorkspace.getWorkspaceName()).thenReturn(createDTO.getWorkspaceName());
        when(mockWorkspace.getWorkspaceSubname()).thenReturn(createDTO.getWorkspaceSubname());
        when(mockWorkspace.getCreatedAt()).thenReturn(LocalDateTime.now());

        // when
        WorkspaceResponse.SimpleDTO result = workspaceService.createWorkspace(createDTO, userId);

        // then
        // 1. 결과가 null이 아님을 확인
        assertNotNull(result);
        // 2. ID 일치 확인
        assertEquals(1, result.getWorkspaceId());
        // 3. 이름 일치 확인
        assertEquals("테스트 워크스페이스", result.getWorkspaceName());

        // 4. 메소드 호출 횟수 검증 - save가 정확히 1번 호출되었는지 확인
        verify(workspaceRepository, times(1)).save(any(Workspace.class));
    }

    @Test
    @DisplayName("워크스페이스 생성 실패 테스트 - 존재하지 않는 사용자")
    void createWorkspace_Fail_UserNotFound() {
        // given
        // 1. 존재하지 않는 사용자 ID 설정
        Integer userId = 999;
        // 2. 워크스페이스 생성 요청 DTO 생성
        WorkspaceRequest.CreateDTO createDTO = WorkspaceRequest.CreateDTO.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("홍길동")
                .representerPhoneNumber("010-1234-5678")
                .companyName("테스트 회사")
                .build();

        // 3. Mock 설정 - 사용자 조회 시 빈 Optional 반환 (사용자 없음)
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        // 실제 테스트할 메소드 호출 (예외 발생 예상)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.createWorkspace(createDTO, userId));

        // then
        // 1. 예외 메시지 확인
        assertEquals("해당 사용자를 찾을 수 없습니다. ID: " + userId, exception.getMessage());
        // 2. save 메소드가 호출되지 않았음을 확인 (실패 시 저장하면 안 됨)
        verify(workspaceRepository, never()).save(any(Workspace.class)); // save는 절대 호출되면 안 됨
    }

    @Test
    @DisplayName("워크스페이스 생성 실패 테스트 - 중복된 URL")
    void createWorkspace_Fail_DuplicateUrl() {
        // given
        // 1. 유효한 사용자 ID 설정
        Integer userId = 1;
        // 2. 중복될 URL을 포함한 워크스페이스 생성 요청 DTO 생성
        WorkspaceRequest.CreateDTO createDTO = WorkspaceRequest.CreateDTO.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("duplicate-url")
                .representerName("홍길동")
                .representerPhoneNumber("010-1234-5678")
                .companyName("테스트 회사")
                .build();

        // 3. Mock 객체 생성
        User mockUser = mock(User.class);

        // 4. Mock 설정 - 사용자는 존재하지만 URL이 중복
        //    4-1. 사용자 조회 성공
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        //    4-2. URL 중복 존재
        when(workspaceRepository.existsByWorkspaceUrl(createDTO.getWorkspaceUrl())).thenReturn(true);

        // when
        // 실제 테스트할 메소드 호출 (예외 발생 예상)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.createWorkspace(createDTO, userId));

        // then
        // 1. 예외 메시지 확인
        assertEquals("이미 사용 중인 URL입니다. 다른 URL을 입력해주세요.", exception.getMessage());
        // 2. save 메소드가 호출되지 않았음을 확인 (실패 시 저장하면 안 됨)
        verify(workspaceRepository, never()).save(any(Workspace.class));
    }
}