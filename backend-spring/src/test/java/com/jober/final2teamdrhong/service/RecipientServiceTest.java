package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.recipient.RecipientRequest;
import com.jober.final2teamdrhong.dto.recipient.RecipientResponse;
import com.jober.final2teamdrhong.entity.Recipient;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.RecipientRepository;
import com.jober.final2teamdrhong.service.validator.WorkspaceValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipientServiceTest {

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private WorkspaceValidator workspaceValidator;

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

        // 2. 의존성 메서드가 반환할 가짜(Mock) 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        Recipient savedRecipient = Recipient.builder()
                .recipientId(1) // DTO 변환 시 필요한 ID 값 설정
                .recipientName(createDTO.getRecipientName())
                .recipientPhoneNumber(createDTO.getRecipientPhoneNumber())
                .recipientMemo(createDTO.getRecipientMemo())
                .workspace(mockWorkspace)
                .build();

        // 3. Mockito 행동 정의
        //    - workspaceValidator.validateAndGetWorkspace 호출 시, mockWorkspace를 반환하여 권한 검증을 통과시킵니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId)).thenReturn(mockWorkspace);
        //    - recipientRepository.save 호출 시, 저장된 것처럼 savedRecipient 객체를 반환합니다.
        when(recipientRepository.save(any(Recipient.class))).thenReturn(savedRecipient);

        // when
        RecipientResponse.SimpleDTO result = recipientService.createRecipient(createDTO, workspaceId, userId);

        // then
        // 1. 반환된 DTO가 null이 아닌지, 그리고 필드 값들이 요청한 데이터와 일치하는지 검증합니다.
        assertNotNull(result);
        assertEquals(1, result.getRecipientId());
        assertEquals("홍길동", result.getRecipientName());
        assertEquals("010-1234-5678", result.getRecipientPhoneNumber());
        assertEquals("테스트 메모", result.getRecipientMemo());

        // 2. 의존성 객체의 메서드들이 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
        verify(recipientRepository, times(1)).save(any(Recipient.class));
    }

    @Test
    @DisplayName("수신자 생성 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void createRecipient_Fail_WorkspaceNotFoundOrUnauthorized_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer userId = 1;
        Integer nonExistingWorkspaceId = 999; // 존재하지 않거나 권한 없는 ID를 대표
        RecipientRequest.CreateDTO createDTO = new RecipientRequest.CreateDTO();

        // 2. Mockito 행동 정의: workspaceValidator가 예외를 던지도록 설정합니다.
        when(workspaceValidator.validateAndGetWorkspace(nonExistingWorkspaceId, userId))
                .thenThrow(new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + nonExistingWorkspaceId));

        // when
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                recipientService.createRecipient(createDTO, nonExistingWorkspaceId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + nonExistingWorkspaceId,
                thrown.getMessage());

        // 2. (Quality) 검증 실패 시, DB 저장 로직이 실행되지 않았음을 검증합니다.
        verify(recipientRepository, never()).save(any(Recipient.class));
    }
}
