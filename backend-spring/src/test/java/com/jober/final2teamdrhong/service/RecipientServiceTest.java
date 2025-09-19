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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("수신자 목록 페이징 조회 성공 테스트")
    void readRecipients_Paging_Success_Test() {
        // given
        // 1. 서비스 메소드에 넘겨줄 ID와 Pageable 객체를 준비합니다.
        Integer workspaceId = 1;
        Integer userId = 1;
        Pageable pageable = PageRequest.of(0, 10);
        Workspace mockWorkspace = mock(Workspace.class);

        List<Recipient> recipientList = List.of(
                Recipient.builder()
                        .recipientName("홍길동")
                        .recipientPhoneNumber("010-1111-1111")
                        .workspace(mockWorkspace)
                        .build(),
                Recipient.builder()
                        .recipientName("임꺽정")
                        .recipientPhoneNumber("010-2222-2222")
                        .workspace(mockWorkspace)
                        .build()
        );
        Page<Recipient> recipientPage = new PageImpl<>(recipientList, pageable, 2);

        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId)).thenReturn(mockWorkspace);
        when(recipientRepository.findAllByWorkspace_WorkspaceId(workspaceId, pageable)).thenReturn(recipientPage);

        // when
        Page<RecipientResponse.SimpleDTO> resultPage = recipientService.readRecipients(workspaceId, userId, pageable);

        // then
        // 1. 반환된 Page 객체의 주요 정보들을 검증합니다.
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent().size()).isEqualTo(2);
        // 2. Page에 담긴 내용(DTO)을 검증합니다.
        assertThat(resultPage.getContent()).extracting(RecipientResponse.SimpleDTO::getRecipientName)
                .containsExactly("홍길동", "임꺽정");

        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
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

        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenThrow(new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + workspaceId));

        // when
        Throwable thrown = assertThrows(IllegalArgumentException.class,
                () -> recipientService.readRecipients(workspaceId, userId, pageable));

        // then
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + workspaceId, thrown.getMessage());
        verify(recipientRepository, never()).findAllByWorkspace_WorkspaceId(anyInt(), any(Pageable.class));
    }
}