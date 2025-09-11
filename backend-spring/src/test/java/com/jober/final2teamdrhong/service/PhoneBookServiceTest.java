package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.phonebook.PhoneBookRequest;
import com.jober.final2teamdrhong.dto.phonebook.PhoneBookResponse;
import com.jober.final2teamdrhong.entity.PhoneBook;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.PhoneBookRepository;
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
class PhoneBookServiceTest {

    @Mock
    private PhoneBookRepository phoneBookRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private PhoneBookService phoneBookService;

    @Test
    @DisplayName("주소록 생성 성공 테스트")
    void createPhoneBook_Success_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer userId = 1;
        Integer workspaceId = 1;
        PhoneBookRequest.CreateDTO createDTO = PhoneBookRequest.CreateDTO.builder()
                .phoneBookName("테스트 주소록")
                .phoneBookMemo("테스트 메모입니다.")
                .build();

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        // PhoneBook은 실제 객체를 사용하여 DTO 변환을 테스트합니다.
        PhoneBook phoneBookToSave = PhoneBook.builder()
                .phoneBookName(createDTO.getPhoneBookName())
                .phoneBookMemo(createDTO.getPhoneBookMemo())
                .workspace(mockWorkspace)
                .build();

        // 3. Mockito 행동 정의
        //    - findByWorkspaceIdAndUser_UserId 호출 시, mockWorkspace를 반환하여 권한 검증을 통과시킵니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(workspaceId, userId))
                .thenReturn(Optional.of(mockWorkspace));
        //    - phoneBookRepository.save 호출 시, 저장된 것처럼 phoneBookToSave 객체를 반환합니다.
        when(phoneBookRepository.save(any(PhoneBook.class))).thenReturn(phoneBookToSave);

        // when
        // 실제 테스트 대상인 서비스 메소드를 호출합니다.
        PhoneBookResponse.SimpleDTO result = phoneBookService.createPhoneBook(createDTO, workspaceId, userId);

        // then
        // 1. 반환된 DTO가 null이 아닌지, 그리고 필드 값들이 요청한 데이터와 일치하는지 검증합니다.
        assertNotNull(result);
        assertEquals("테스트 주소록", result.getPhoneBookName());
        assertEquals("테스트 메모입니다.", result.getPhoneBookMemo());

        // 2. Repository의 find와 save 메소드가 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceRepository, times(1)).findByWorkspaceIdAndUser_UserId(workspaceId, userId);
        verify(phoneBookRepository, times(1)).save(any(PhoneBook.class));
    }

    @Test
    @DisplayName("주소록 생성 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void createPhoneBook_Fail_UnauthorizedWorkspace_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer userId = 1;
        Integer unauthorizedWorkspaceId = 999;
        PhoneBookRequest.CreateDTO createDTO = new PhoneBookRequest.CreateDTO();

        // 2. Mockito 행동 정의: Repository가 이 ID로 조회 시 "결과 없음"을 의미하는
        //    비어있는 Optional을 반환하도록 설정합니다.
        when(workspaceRepository.findByWorkspaceIdAndUser_UserId(unauthorizedWorkspaceId, userId))
                .thenReturn(Optional.empty());

        // when
        // 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.createPhoneBook(createDTO, unauthorizedWorkspaceId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId,
                thrown.getMessage());

        // 2. (중요) 로직이 초반에 중단되었으므로, 주소록을 저장하는 로직은 절대 호출되면 안됩니다.
        verify(phoneBookRepository, never()).save(any(PhoneBook.class));
    }
}
