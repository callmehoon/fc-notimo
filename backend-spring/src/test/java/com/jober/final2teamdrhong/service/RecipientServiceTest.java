package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.recipient.RecipientRequest;
import com.jober.final2teamdrhong.dto.recipient.RecipientResponse;
import com.jober.final2teamdrhong.entity.Recipient;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.RecipientRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipientServiceTest {

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private RecipientService recipientService;

    @Test
    @DisplayName("수신자 생성 성공 테스트")
    void createRecipient_Success_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer userId = 1;
        Integer workspaceId = 1;
        RecipientRequest.CreateDTO createDTO = RecipientRequest.CreateDTO.builder()
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .recipientMemo("테스트 메모")
                .build();

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        // Recipient는 실제 객체를 사용하여 DTO 변환을 테스트합니다.
        Recipient recipientToSave = Recipient.builder()
                .recipientName(createDTO.getRecipientName())
                .recipientPhoneNumber(createDTO.getRecipientPhoneNumber())
                .recipientMemo(createDTO.getRecipientMemo())
                .workspace(mockWorkspace)
                .build();

        // 3. Mockito 행동 정의
        //    - findByWorkspaceIdAndUser_UserId 호출 시, mockWorkspace를 반환하여 권한 검증을 통과시킵니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.of(mockWorkspace));
        //    - recipientRepository.save 호출 시, 저장된 것처럼 recipientToSave 객체를 반환합니다.
        when(recipientRepository.save(any(Recipient.class))).thenReturn(recipientToSave);

        // when
        // 실제 테스트 대상인 서비스 메소드를 호출합니다.
        RecipientResponse.SimpleDTO result = recipientService.createRecipient(createDTO, workspaceId, userId);

        // then
        // 1. 반환된 DTO가 null이 아닌지, 그리고 필드 값들이 요청한 데이터와 일치하는지 검증합니다.
        assertNotNull(result);
        assertEquals("홍길동", result.getRecipientName());
        assertEquals("010-1234-5678", result.getRecipientPhoneNumber());
        assertEquals("테스트 메모", result.getRecipientMemo());

        // 2. Repository의 find와 save 메소드가 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceRepository, times(1)).findByWorkspaceIdAndUser_UserId(workspaceId, userId);
        verify(recipientRepository, times(1)).save(any(Recipient.class));
    }

    @Test
    @DisplayName("수신자 생성 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void createRecipient_Fail_NotFoundOrUnauthorized_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer userId = 1;
        Integer nonExistingWorkspaceId = 999; // 존재하지 않거나 권한 없는 ID를 대표
        RecipientRequest.CreateDTO createDTO = new RecipientRequest.CreateDTO();

        // 2. Mockito 행동 정의: Repository가 이 ID로 조회 시 "결과 없음"을 의미하는
        //    비어있는 Optional을 반환하도록 설정합니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(nonExistingWorkspaceId, userId))
                .thenReturn(Optional.empty());

        // when
        // 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                recipientService.createRecipient(createDTO, nonExistingWorkspaceId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + nonExistingWorkspaceId,
                thrown.getMessage());

        // 2. (중요) 로직이 초반에 중단되었으므로, 수신자를 저장하는 로직은 절대 호출되면 안됩니다.
        verify(recipientRepository, never()).save(any(Recipient.class));
    }

    @Test
    @DisplayName("수신자 목록 페이징 조회 성공 테스트")
    void readRecipients_Paging_Success_Test() {
        // given
        // 1. 서비스 메소드에 넘겨줄 ID와 Pageable 객체를 준비합니다.
        Integer workspaceId = 1;
        Integer userId = 1;
        Pageable pageable = PageRequest.of(0, 10); // 0번째 페이지, 10개씩

        // 2. Repository가 반환할 가짜 데이터(Mock)를 생성합니다.
        User mockUser = User.builder()
                .userName("테스트유저")
                .userEmail("test@example.com")
                .userNumber("010-1234-5678")
                .build();
        Workspace mockWorkspace = Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("테스트대표")
                .representerPhoneNumber("010-0000-0000")
                .companyName("테스트회사")
                .user(mockUser)
                .build();

        // 3. Mockito는 Page 인터페이스를 직접 만들 수 없으므로, 실제 구현체인 PageImpl을 사용해 Page 객체를 만듭니다.
        //    이때, content로 사용될 List는 변수에 담지 않고 직접 생성자에 넣어줍니다.
        Page<Recipient> recipientPage = new PageImpl<>(
                List.of(
                        Recipient.builder().recipientName("홍길동").recipientPhoneNumber("010-1111-1111").workspace(mockWorkspace).build(),
                        Recipient.builder().recipientName("임꺽정").recipientPhoneNumber("010-1111-1111").workspace(mockWorkspace).build()
                ),
                pageable,
                2
        );

        // 4. Mockito 행동 정의
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.of(mockWorkspace));
        when(recipientRepository.findAllByWorkspace_WorkspaceId(workspaceId, pageable))
                .thenReturn(recipientPage);

        // when
        // 실제 테스트 대상인 서비스 메소드를 호출합니다.
        Page<RecipientResponse.SimpleDTO> resultPage = recipientService.readRecipients(workspaceId, userId, pageable);

        // then
        // 1. 반환된 Page 객체의 주요 정보들을 검증합니다.
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent().size()).isEqualTo(2);
        // 2. Page에 담긴 내용(DTO)을 검증합니다.
        assertThat(resultPage.getContent()).extracting(RecipientResponse.SimpleDTO::getRecipientName)
                .containsExactly("홍길동", "임꺽정");

        // 3. 각 Repository의 메소드가 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceRepository, times(1)).findByWorkspaceIdAndUser_UserId(workspaceId, userId);
        verify(recipientRepository, times(1)).findAllByWorkspace_WorkspaceId(workspaceId, pageable);
    }

    @Test
    @DisplayName("수신자 목록 페이징 조회 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void readRecipients_Paging_Fail_Unauthorized_Test() {
        // given
        // 1. 존재하지 않거나 권한이 없는 workspaceId와 Pageable 객체를 준비합니다.
        Integer workspaceId = 999;
        Integer userId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        // 2. Mockito 행동 정의: 권한 검증 단계에서 비어있는 Optional을 반환하도록 설정합니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.empty());

        // when
        // 서비스 메소드를 호출했을 때 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class,
                () -> recipientService.readRecipients(workspaceId, userId, pageable));

        // then
        // 1. 발생한 예외의 메시지가 예상과 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + workspaceId,
                thrown.getMessage());

        // 2. (중요) 권한 검증에서 실패했으므로, 수신자 목록을 조회하는 메소드는 절대 호출되면 안됩니다.
        //    any(Pageable.class)를 사용하여 Pageable 타입의 어떤 객체와도 매칭되도록 합니다.
        verify(recipientRepository, never()).findAllByWorkspace_WorkspaceId(anyInt(),
                any(Pageable.class));
    }

    @Test
    @DisplayName("수신자 정보 수정 성공 테스트")
    void updateRecipient_Success_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer userId = 1;
        Integer workspaceId = 1;
        Integer recipientId = 1;
        RecipientRequest.UpdateDTO updateDTO = RecipientRequest.UpdateDTO.builder()
                .newRecipientName("김길동")
                .newRecipientPhoneNumber("010-9999-8888")
                .newRecipientMemo("수정된 메모")
                .build();

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        // findByRecipientIdAndWorkspace_WorkspaceId가 반환할 실제 Recipient 객체를 생성합니다.
        // 이 객체의 상태가 서비스 메소드 호출 후 어떻게 변하는지 검증해야 합니다.
        Recipient existingRecipient = Recipient.builder()
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1111-2222")
                .recipientMemo("원본 메모")
                .workspace(mockWorkspace)
                .build();
        // updatedAt 필드는 @UpdateTimestamp에 의해 관리되지만, 테스트에서는 직접 값을 넣어줍니다.
        existingRecipient.setUpdatedAt(java.time.LocalDateTime.now().minusDays(1));
        java.time.LocalDateTime beforeUpdate = existingRecipient.getUpdatedAt();

        // 3. Mockito 행동 정의
        //    - 워크스페이스 권한 검증을 통과시킵니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.of(mockWorkspace));
        //    - 수신자 조회 및 소유권 검증을 통과시키고, 위에서 만든 existingRecipient 객체를 반환합니다.
        when(recipientRepository.findByRecipientIdAndWorkspace_WorkspaceId(recipientId, workspaceId))
                .thenReturn(Optional.of(existingRecipient));

        // when
        // 실제 테스트 대상인 서비스 메소드를 호출합니다.
        RecipientResponse.SimpleDTO result = recipientService.updateRecipient(updateDTO, workspaceId, recipientId, userId);

        // then
        // 1. 반환된 DTO의 필드 값들이 요청한 데이터(updateDTO)와 일치하는지 검증합니다.
        assertNotNull(result);
        assertEquals(updateDTO.getNewRecipientName(), result.getRecipientName());
        assertEquals(updateDTO.getNewRecipientPhoneNumber(), result.getRecipientPhoneNumber());
        assertEquals(updateDTO.getNewRecipientMemo(), result.getRecipientMemo());
        // 1-1. updatedAt이 수정되었는지 검증합니다.
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(beforeUpdate));


        // 2. (중요) 서비스 계층의 핵심 로직 검증:
        //    메소드에 전달했던 existingRecipient 객체의 상태가 DTO의 내용대로 실제로 변경되었는지 확인합니다.
        //    이는 @Transactional에 의한 Dirty Checking이 올바르게 동작할 것임을 보장합니다.
        assertEquals(updateDTO.getNewRecipientName(), existingRecipient.getRecipientName());
        assertEquals(updateDTO.getNewRecipientPhoneNumber(), existingRecipient.getRecipientPhoneNumber());
        assertEquals(updateDTO.getNewRecipientMemo(), existingRecipient.getRecipientMemo());
        // 2-1. 엔티티의 updatedAt 필드 또한 수정되었는지 검증합니다.
        assertNotNull(existingRecipient.getUpdatedAt());
        assertTrue(existingRecipient.getUpdatedAt().isAfter(beforeUpdate));

        // 3. 각 Repository의 메소드가 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceRepository, times(1)).findByWorkspaceIdAndUser_UserId(workspaceId, userId);
        verify(recipientRepository, times(1)).findByRecipientIdAndWorkspace_WorkspaceId(recipientId, workspaceId);
    }

    @Test
    @DisplayName("수신자 정보 수정 실패 테스트 - 존재하지 않는 수신자")
    void updateRecipient_Fail_RecipientNotFound_Test() {
        // given
        // 1. 존재하지 않는 recipientId를 준비합니다.
        Integer userId = 1;
        Integer workspaceId = 1;
        Integer nonExistingRecipientId = 999;
        RecipientRequest.UpdateDTO updateDTO = new RecipientRequest.UpdateDTO(); // 내용은 중요하지 않음

        // 2. Mockito 행동 정의
        //    - 워크스페이스 권한 검증은 통과시킵니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.of(mock(Workspace.class)));
        //    - 수신자 조회 단계에서 비어있는 Optional을 반환하여 "결과 없음"을 시뮬레이션합니다.
        when(recipientRepository.findByRecipientIdAndWorkspace_WorkspaceId(nonExistingRecipientId, workspaceId))
                .thenReturn(Optional.empty());

        // when
        // 서비스 메소드를 호출했을 때 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                recipientService.updateRecipient(updateDTO, workspaceId, nonExistingRecipientId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 일치하는지 확인합니다.
        assertEquals("해당 워크스페이스에 존재하지 않는 수신자입니다. ID: " + nonExistingRecipientId,
                thrown.getMessage());
    }

    @Test
    @DisplayName("수신자 정보 수정 실패 테스트 - 권한이 없는 워크스페이스")
    void updateRecipient_Fail_UnauthorizedWorkspace_Test() {
        // given
        // 1. 권한 없는 workspaceId를 준비합니다.
        Integer userId = 1;
        Integer unauthorizedWorkspaceId = 999;
        Integer recipientId = 1;
        RecipientRequest.UpdateDTO updateDTO = new RecipientRequest.UpdateDTO();

        // 2. Mockito 행동 정의: 워크스페이스 권한 검증 단계에서 실패하도록 설정합니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(unauthorizedWorkspaceId, userId))
                .thenReturn(Optional.empty());

        // when
        // 서비스 메소드를 호출했을 때 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                recipientService.updateRecipient(updateDTO, unauthorizedWorkspaceId, recipientId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId,
                thrown.getMessage());

        // 2. (중요) 권한 검증에서 실패했으므로, 수신자를 조회하는 로직은 절대 호출되면 안됩니다.
        verify(recipientRepository, never()).findByRecipientIdAndWorkspace_WorkspaceId(anyInt(),
                anyInt());
    }
}