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
        when(mockWorkspace.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(mockWorkspace.getDeletedAt()).thenReturn(null);

        // when
        WorkspaceResponse.SimpleDTO result = workspaceService.createWorkspace(createDTO, userId);

        // then
        // 1. 결과가 null이 아님을 확인
        assertNotNull(result);
        // 2. ID 일치 확인
        assertEquals(1, result.getWorkspaceId());
        // 3. 이름 일치 확인
        assertEquals("테스트 워크스페이스", result.getWorkspaceName());
        // 4. 시간 필드 검증
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertNull(result.getDeletedAt());

        // 5. 메소드 호출 횟수 검증 - save가 정확히 1번 호출되었는지 확인
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

        testWorkspace1.setCreatedAt(LocalDateTime.now());
        testWorkspace1.setUpdatedAt(LocalDateTime.now());
        testWorkspace1.setDeletedAt(null);

        testWorkspace2.setCreatedAt(LocalDateTime.now());
        testWorkspace2.setUpdatedAt(LocalDateTime.now());
        testWorkspace2.setDeletedAt(null);

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

        WorkspaceResponse.SimpleDTO firstWorkspace = result.getFirst();
        assertEquals("테스트 워크스페이스1", firstWorkspace.getWorkspaceName());
        assertNotNull(firstWorkspace.getCreatedAt());
        assertNotNull(firstWorkspace.getUpdatedAt());
        assertNull(firstWorkspace.getDeletedAt());

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
        when(mockWorkspace.getUpdatedAt()).thenReturn(now);
        when(mockWorkspace.getDeletedAt()).thenReturn(null);

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
        assertEquals(now, result.getUpdatedAt());
        assertNull(result.getDeletedAt());

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

    @Test
    @DisplayName("워크스페이스 수정 성공 테스트")
    void updateWorkspace_Success() {
        // given
        // 1. 테스트에 사용할 userId와 workspaceId를 준비합니다.
        Integer userId = 1;
        Integer workspaceId = 1;

        // Mock User 객체 생성 추가
        User mockUser = mock(User.class);

        WorkspaceRequest.UpdateDTO updateDTO = WorkspaceRequest.UpdateDTO.builder()
                .newWorkspaceName("수정된 워크스페이스")
                .newWorkspaceUrl("updated-unique-url")
                .newRepresenterName("수정된 대표")
                .newRepresenterPhoneNumber("010-9999-8888")
                .newCompanyName("수정된 회사")
                .build();

        // 원본 Workspace 객체 생성 (Setter가 있으므로 Mock 객체 대신 실제 객체 사용)
        LocalDateTime now = LocalDateTime.now();

        Workspace existingWorkspace = Workspace.builder()
                .workspaceName("원본 워크스페이스")
                .workspaceUrl("original-url")
                .representerName("원본 대표")
                .representerPhoneNumber("010-1111-2222")
                .companyName("원본 회사")
                .user(mockUser)
                .build();

        existingWorkspace.setCreatedAt(now);
        existingWorkspace.setUpdatedAt(now);
        existingWorkspace.setDeletedAt(null);

        // Mockito 행동 정의
        // 1. findByWorkspaceIdAndUser_UserId 가 호출되면 위에서 만든 원본 객체를 반환하도록 설정
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.of(existingWorkspace));
        // 2. 수정하려는 새 URL이 중복되지 않았다고 설정
        when(workspaceRepository.existsByWorkspaceUrl(updateDTO.getNewWorkspaceUrl()))
                .thenReturn(false);

        // when
        WorkspaceResponse.DetailDTO result = workspaceService.updateWorkspace(updateDTO, workspaceId, userId);

        // then
        assertNotNull(result);
        // 1. DTO의 값이 updateDTO의 값으로 잘 변경되었는지 확인
        assertEquals("수정된 워크스페이스", result.getWorkspaceName());
        assertEquals("updated-unique-url", result.getWorkspaceUrl());
        assertNotNull(result.getUpdatedAt());
        assertNull(result.getDeletedAt());

        // 2. 실제 엔티티의 값이 잘 변경되었는지도 확인 (Dirty Checking 검증)
        assertEquals("수정된 워크스페이스", existingWorkspace.getWorkspaceName());
        assertEquals("updated-unique-url", existingWorkspace.getWorkspaceUrl());

        // 3. Repository의 find와 exists 메소드가 각각 1번씩 호출되었는지 검증
        verify(workspaceRepository, times(1)).findByWorkspaceIdAndUser_UserId(workspaceId, userId);
        verify(workspaceRepository, times(1)).existsByWorkspaceUrl(updateDTO.getNewWorkspaceUrl());
    }

    @Test
    @DisplayName("워크스페이스 수정 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void updateWorkspace_Fail_NotFoundOrUnauthorized() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다. DTO의 내용은 중요하지 않습니다.
        Integer userId = 1;
        Integer nonExistingWorkspaceId = 999;
        WorkspaceRequest.UpdateDTO updateDTO = new WorkspaceRequest.UpdateDTO();

        // Mockito 행동 정의: Repository가 이 ID로 조회 시 "결과 없음"을 의미하는
        //    비어있는 Optional을 반환하도록 설정합니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(nonExistingWorkspaceId, userId))
                .thenReturn(Optional.empty());

        // when
        // 1. 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        //    assertThrows는 예외가 발생하면 그 예외 객체를 반환하고, 발생하지 않으면 테스트를 실패시킵니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.updateWorkspace(updateDTO, nonExistingWorkspaceId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + nonExistingWorkspaceId,
                thrown.getMessage());

        // 2. (중요) 로직이 초반에 중단되었으므로, URL 중복을 체크하는 로직은 절대 호출되면 안됩니다.
        //    이를 verify와 never()를 통해 검증합니다.
        verify(workspaceRepository, never()).existsByWorkspaceUrl(anyString());
    }

    @Test
    @DisplayName("워크스페이스 수정 실패 테스트 - 중복된 URL")
    void updateWorkspace_Fail_DuplicateUrl() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer userId = 1;
        Integer workspaceId = 1;

        // Mock User 객체 생성 추가
        User mockUser = mock(User.class);

        WorkspaceRequest.UpdateDTO updateDTO = WorkspaceRequest.UpdateDTO.builder()
                .newWorkspaceName("수정된 워크스페이스")
                .newWorkspaceUrl("duplicate-url")
                .newRepresenterName("수정된 대표")
                .newRepresenterPhoneNumber("010-9999-8888")
                .newCompanyName("수정된 회사")
                .build();

        // 2. 수정 대상이 될 원본 워크스페이스 객체를 준비합니다.
        Workspace existingWorkspace = Workspace.builder()
                .workspaceName("원본 워크스페이스")
                .workspaceUrl("original-url")
                .representerName("원본 대표")
                .representerPhoneNumber("010-1111-2222")
                .companyName("원본 회사")
                .user(mockUser)
                .build();

        // Mockito 행동 정의 (2단계로 이루어짐)
        //  1. 첫 번째 조회(findByWorkspaceIdAndUser_UserId)는 성공해야 하므로, 위에서 만든 원본 객체를 반환하도록 설정합니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.of(existingWorkspace));
        //  2. 두 번째 조회(existsByWorkspaceUrl)는 실패해야 하므로, URL이 이미 존재한다는 의미로 true를 반환하도록 설정합니다.
                when(workspaceRepository.existsByWorkspaceUrl(updateDTO.getNewWorkspaceUrl()))
                .thenReturn(true);

        // when
        // 서비스 메소드를 호출했을 때 예외가 발생하는지 검증하고, 발생한 예외를 캡처합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.updateWorkspace(updateDTO, workspaceId, userId));

        // then
        // 발생한 예외의 메시지가 "URL 중복" 관련 메시지와 일치하는지 확인합니다.
        assertEquals("이미 사용 중인 URL입니다. 다른 URL을 입력해주세요.", thrown.getMessage());
    }

    @Test
    @DisplayName("워크스페이스 삭제 성공 테스트")
    void deleteWorkspace_Success() {
        // given
        // 1. 테스트에 사용할 userId와 workspaceId를 준비합니다.
        Integer userId = 1;
        Integer workspaceId = 1;
        User mockUser = mock(User.class);

        // 2. '실제' Workspace 객체를 생성합니다. ID는 아직 null이므로 Spy 객체를 사용합니다.
        Workspace realWorkspace = Workspace.builder()
                .workspaceName("삭제될 워크스페이스")
                .workspaceUrl("delete-url") // @NonNull 필드 추가
                .representerName("삭제 대표") // @NonNull 필드 추가
                .representerPhoneNumber("010-1111-1111") // @NonNull 필드 추가
                .companyName("삭제 회사") // @NonNull 필드 추가
                .user(mockUser)
                .build();

        // 3. 실제 객체를 기반으로 Spy 객체를 생성합니다.
        Workspace workspaceToDeleteSpy = spy(realWorkspace);

        // 4. DB에서 조회된 것처럼 ID 값을 반환하도록 getWorkspaceId() 메소드의 동작만 가짜로 정의합니다.
        when(workspaceToDeleteSpy.getWorkspaceId()).thenReturn(workspaceId);

        // 5. Repository가 조회 시 이 Spy 객체를 반환하도록 설정합니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.of(workspaceToDeleteSpy));

        // when
        // 실제 테스트할 메소드 호출
        WorkspaceResponse.SimpleDTO result = workspaceService.deleteWorkspace(workspaceId, userId);

        // then
        // 1. 반환된 DTO 검증
        assertNotNull(result);
        assertEquals(workspaceId, result.getWorkspaceId());
        assertEquals("삭제될 워크스페이스", result.getWorkspaceName());
        assertNotNull(result.getDeletedAt());

        // 2. Spy 객체의 상태가 실제로 변경되었는지 검증 (verify는 spy 객체에도 동일하게 사용 가능)
        verify(workspaceToDeleteSpy, times(1)).setDeleted(true);
        verify(workspaceToDeleteSpy, times(1)).setDeletedAt(any(LocalDateTime.class));

        // 3. Repository의 save 메소드가 호출되었는지 검증
        verify(workspaceRepository, times(1)).save(workspaceToDeleteSpy);
    }

    @Test
    @DisplayName("워크스페이스 삭제 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void deleteWorkspace_Fail_NotFoundOrUnauthorized() {
        // given
        // 1. 테스트에 사용할 ID들을 준비합니다.
        Integer userId = 1;
        Integer nonExistingWorkspaceId = 999; // 존재하지 않는 워크스페이스 ID

        // 2. Mock 설정: 워크스페이스 조회 시 "결과 없음"을 의미하는 빈 Optional 반환
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(nonExistingWorkspaceId, userId))
                .thenReturn(Optional.empty());

        // when
        // 실제 테스트할 메소드 호출 (예외 발생 예상)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                workspaceService.deleteWorkspace(nonExistingWorkspaceId, userId));

        // then
        // 1. 예외 메시지 확인
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + nonExistingWorkspaceId,
                exception.getMessage());

        // 2. save 메소드가 호출되지 않았는지 확인 (실패 시 저장하면 안 됨)
        verify(workspaceRepository, never()).save(any(Workspace.class));
    }
}
