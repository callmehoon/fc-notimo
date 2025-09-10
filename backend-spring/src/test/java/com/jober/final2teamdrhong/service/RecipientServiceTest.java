package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.recipient.RecipientRequest;
import com.jober.final2teamdrhong.dto.recipient.RecipientResponse;
import com.jober.final2teamdrhong.entity.Recipient;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.RecipientRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
}
