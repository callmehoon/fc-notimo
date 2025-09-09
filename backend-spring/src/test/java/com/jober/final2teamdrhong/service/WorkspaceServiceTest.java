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
import java.util.List;
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

    @Test
    @DisplayName("워크스페이스 목록 조회 성공 테스트")
    void readWorkspaces_Success() {
        // given
        // 1. 서비스 메소드에 넘겨줄 userId를 준비합니다.
        Integer userId = 1;

        // 2. 서비스 메소드 내부의 userRepository.findById(userId) 호출을 통과시키기 위한 가짜 User 객체를 만듭니다.
        //    내용물은 중요하지 않습니다. Optional.empty()가 아니기만 하면 됩니다.
        User mockUser = mock(User.class);

        // 3. workspaceRepository가 최종적으로 반환할 "결과물(가짜 데이터 리스트)"을 미리 만들어둡니다.
        //    이 객체들은 실제 DB와 아무 관련이 없는, 순수한 Java 객체일 뿐입니다.
        Workspace testWorkspace1 = Workspace.builder()
                .workspaceName("테스트 워크스페이스1")
                .workspaceUrl("test-url-1")
                .representerName("테스트대표1")
                .representerPhoneNumber("010-1111-1111")
                .companyName("테스트회사1")
                .user(mockUser)
                .build();
        Workspace testWorkspace2 = Workspace.builder()
                .workspaceName("테스트 워크스페이스2")
                .workspaceUrl("test-url-2")
                .representerName("테스트대표1")
                .representerPhoneNumber("010-1111-1111")
                .companyName("테스트회사2")
                .user(mockUser)
                .build();
        List<Workspace> mockWorkspaceList = List.of(testWorkspace1, testWorkspace2);

        // 4. Mockito 행동 정의: Repository 메소드가 호출될 때 어떤 값을 반환할지 설정
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(workspaceRepository.findAllByUser_UserId(userId)).thenReturn(mockWorkspaceList);

        // when
        // 실제 테스트 대상인 서비스 메소드 호출
        List<WorkspaceResponse.SimpleDTO> result = workspaceService.readWorkspaces(userId);

        // then
        // 1. 결과 검증
        assertNotNull(result);
        assertEquals(2, result.size()); // DTO 리스트의 크기가 2인지 확인
        assertEquals("테스트 워크스페이스1", result.getFirst().getWorkspaceName()); // DTO의 내용이 올바른지 확인

        // 2. 특정 메소드가 정확히 1번씩 호출되었는지 검증
        verify(userRepository, times(1)).findById(userId);
        verify(workspaceRepository, times(1)).findAllByUser_UserId(userId);
    }

    @Test
    @DisplayName("워크스페이스 목록 조회 실패 테스트 - 존재하지 않는 사용자")
    void readWorkspaces_Fail_UserNotFound() {
        // given
        // 1. 존재하지 않는 사용자 ID를 준비합니다.
        Integer nonExistingUserId = 999;

        // 2. Mockito 행동 정의: userRepository.findById()가 이 ID로 호출되면,
        //    결과가 없다는 의미로 비어있는 Optional을 반환하도록 설정합니다.
        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        // when
        // 예외 발생을 예상하는 로직을 실행하고, 발생한 예외를 캡처
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.readWorkspaces(nonExistingUserId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 일치하는지 확인
        assertEquals("해당 사용자를 찾을 수 없습니다. ID: " + nonExistingUserId, thrown.getMessage());

        // 2. 예외가 발생했으므로, workspaceRepository의 어떤 메소드도 호출되면 안 됨을 검증
        verify(workspaceRepository, never()).findAllByUser_UserId(anyInt());
    }

    @Test
    @DisplayName("워크스페이스 상세 조회 성공 테스트")
    void readWorkspaceDetail_Success() {
        // given
        // 1. 테스트에 사용할 userId와 workspaceId를 준비합니다.
        Integer userId = 1;
        Integer workspaceId = 1;

        // 2. Repository가 반환할 가짜(Mock) Workspace 객체를 생성합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        LocalDateTime now = LocalDateTime.now(); // createdAt 필드용 시간 데이터

        // 3. Mockito 행동 정의: Repository의 findByWorkspaceIdAndUser_UserId 메소드가 호출되면,
        //    위에서 만든 가짜 객체를 Optional에 담아 반환하도록 설정합니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.of(mockWorkspace));

        // 4. (중요) Service가 DTO를 생성할 때 mockWorkspace 객체의 getter들을 호출하게 되므로,
        //    각 getter가 어떤 값을 반환할지 미리 전부 정의해줍니다.
        when(mockWorkspace.getWorkspaceId()).thenReturn(workspaceId);
        when(mockWorkspace.getWorkspaceName()).thenReturn("상세조회 워크스페이스");
        when(mockWorkspace.getWorkspaceSubname()).thenReturn("부이름");
        when(mockWorkspace.getWorkspaceAddress()).thenReturn("주소");
        when(mockWorkspace.getWorkspaceDetailAddress()).thenReturn("상세주소");
        when(mockWorkspace.getWorkspaceUrl()).thenReturn("detail-url");
        when(mockWorkspace.getRepresenterName()).thenReturn("대표");
        when(mockWorkspace.getRepresenterPhoneNumber()).thenReturn("010-1234-5678");
        when(mockWorkspace.getRepresenterEmail()).thenReturn("re@presenter.com");
        when(mockWorkspace.getCompanyName()).thenReturn("회사이름");
        when(mockWorkspace.getCompanyRegisterNumber()).thenReturn("123-45-67890");
        when(mockWorkspace.getCreatedAt()).thenReturn(now);

        // when
        // 실제 테스트 대상인 서비스 메소드를 호출합니다.
        WorkspaceResponse.DetailDTO result = workspaceService.readWorkspaceDetail(workspaceId, userId);

        // then
        // 1. 반환된 DTO가 null이 아닌지, 그리고 모든 필드의 값이
        //    given 단계에서 설정한 값들과 정확히 일치하는지 하나씩 모두 검증합니다.
        assertNotNull(result);
        assertEquals(workspaceId, result.getWorkspaceId());
        assertEquals("상세조회 워크스페이스", result.getWorkspaceName());
        assertEquals("부이름", result.getWorkspaceSubname());
        assertEquals("주소", result.getWorkspaceAddress());
        assertEquals("상세주소", result.getWorkspaceDetailAddress());
        assertEquals("detail-url", result.getWorkspaceUrl());
        assertEquals("대표", result.getRepresenterName());
        assertEquals("010-1234-5678", result.getRepresenterPhoneNumber());
        assertEquals("re@presenter.com", result.getRepresenterEmail());
        assertEquals("회사이름", result.getCompanyName());
        assertEquals("123-45-67890", result.getCompanyRegisterNumber());
        assertEquals(now, result.getCreatedAt());

        // 2. Repository의 findBy.. 메소드가 정확히 1번 호출되었는지 검증합니다.
        verify(workspaceRepository, times(1))
                .findByWorkspaceIdAndUser_UserId(workspaceId, userId);
    }

    @Test
    @DisplayName("워크스페이스 상세 조회 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void readWorkspaceDetail_Fail_NotFound() {
        // given
        // 1. 테스트에 사용할 userId와, 존재하지 않거나 권한이 없는 workspaceId를 준비합니다.
        Integer userId = 1;
        Integer nonExistingWorkspaceId = 999;

        // 2. Mockito 행동 정의: Repository의 findByWorkspaceIdAndUser_UserId 메소드가
        //    호출되면 "결과 없음"을 의미하는 비어있는 Optional을 반환하도록 설정합니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(nonExistingWorkspaceId, userId))
                .thenReturn(Optional.empty());

        // when
        // 예외 발생을 예상하는 서비스 메소드를 호출하고, 발생한 예외를 캡처합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.readWorkspaceDetail(nonExistingWorkspaceId, userId));

        // then
        // 1. 발생한 예외에 담긴 메시지가 서비스 코드에 정의된 것과 정확히 일치하는지 검증합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " +
                nonExistingWorkspaceId, thrown.getMessage());

        // 2. (중요) 예외가 발생하기 전에, Repository의 findBy.. 메소드가 정확히 1번 호출되었는지 검증합니다.
        verify(workspaceRepository, times(1))
                .findByWorkspaceIdAndUser_UserId(nonExistingWorkspaceId, userId);
    }
}