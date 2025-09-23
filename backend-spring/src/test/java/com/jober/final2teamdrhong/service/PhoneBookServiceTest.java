package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.phonebook.PhoneBookRequest;
import com.jober.final2teamdrhong.dto.phonebook.PhoneBookResponse;
import com.jober.final2teamdrhong.dto.recipient.RecipientResponse;
import com.jober.final2teamdrhong.entity.GroupMapping;
import com.jober.final2teamdrhong.entity.PhoneBook;
import com.jober.final2teamdrhong.entity.Recipient;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.GroupMappingRepository;
import com.jober.final2teamdrhong.repository.PhoneBookRepository;
import com.jober.final2teamdrhong.service.validator.PhoneBookValidator;
import com.jober.final2teamdrhong.service.validator.RecipientValidator;
import com.jober.final2teamdrhong.service.validator.WorkspaceValidator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhoneBookServiceTest {

    @Mock
    private PhoneBookRepository phoneBookRepository;

    @Mock
    private GroupMappingRepository groupMappingRepository;

    @Mock
    private WorkspaceValidator workspaceValidator;

    @Mock
    private PhoneBookValidator phoneBookValidator;

    @Mock
    private RecipientValidator recipientValidator;

    @Mock
    private EntityManager entityManager;

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
        PhoneBook phoneBookToSave = PhoneBook.builder()
                .phoneBookId(1) // DTO 변환을 위해 ID 설정
                .phoneBookName(createDTO.getPhoneBookName())
                .phoneBookMemo(createDTO.getPhoneBookMemo())
                .workspace(mockWorkspace)
                .build();

        // 3. Mockito 행동 정의
        //    - workspaceValidator.validateAndGetWorkspace 호출 시, mockWorkspace를 반환하여 권한 검증을 통과시킵니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        //    - phoneBookRepository.save 호출 시, 저장된 것처럼 phoneBookToSave 객체를 반환합니다.
        when(phoneBookRepository.save(any(PhoneBook.class)))
                .thenReturn(phoneBookToSave);

        // when
        // 1. 실제 테스트 대상인 서비스 메소드를 호출합니다.
        PhoneBookResponse.SimpleDTO result = phoneBookService.createPhoneBook(createDTO, workspaceId, userId);

        // then
        // 1. 반환된 DTO가 null이 아닌지, 그리고 필드 값들이 요청한 데이터와 일치하는지 검증합니다.
        assertNotNull(result);
        assertEquals(1, result.getPhoneBookId());
        assertEquals("테스트 주소록", result.getPhoneBookName());
        assertEquals("테스트 메모입니다.", result.getPhoneBookMemo());

        // 2. Validator와 Repository의 메소드가 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
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

        // 2. Mockito 행동 정의: workspaceValidator가 예외를 던지도록 설정합니다.
        when(workspaceValidator.validateAndGetWorkspace(unauthorizedWorkspaceId, userId))
                .thenThrow(new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId));

        // when
        // 1. 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.createPhoneBook(createDTO, unauthorizedWorkspaceId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId,
                thrown.getMessage());

        // 2. (중요) 로직이 초반에 중단되었으므로, 주소록을 저장하는 로직은 절대 호출되면 안됩니다.
        verify(phoneBookRepository, never()).save(any(PhoneBook.class));
    }

    @Test
    @DisplayName("주소록에 수신자 일괄 추가 성공 테스트 - 신규 수신자만 추가")
    void addRecipientsToPhoneBook_Success_AddNewRecipients_Test() {
        // given
        // 1. 테스트용 ID와 요청 DTO를 정의합니다. (수신자 ID 2, 3 추가 요청)
        Integer workspaceId = 1, phoneBookId = 1, userId = 1;
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(List.of(2, 3));

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook mockPhoneBook = PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("테스트 주소록")
                .workspace(mockWorkspace)
                .build();
        List<Recipient> mockRecipients = List.of(
                Recipient.builder()
                        .recipientId(2)
                        .recipientName("홍길동")
                        .recipientPhoneNumber("010-1111-1111")
                        .workspace(mockWorkspace)
                        .build(),
                Recipient.builder()
                        .recipientId(3)
                        .recipientName("임꺽정")
                        .recipientPhoneNumber("010-1111-2222")
                        .workspace(mockWorkspace)
                        .build()
        );
        List<Integer> existingRecipientIds = List.of(1); // 기존에는 1번 수신자만 존재한다고 가정

        // 3. Mock Validator들의 동작을 정의합니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace); // 워크스페이스 검증 통과
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(mockPhoneBook); // 주소록 검증 통과
        when(recipientValidator.validateAndGetRecipients(workspaceId, requestDTO.getRecipientIds()))
                .thenReturn(mockRecipients); // 수신자 검증 통과

        // 4. Mock Repository들의 동작을 정의합니다.
        when(groupMappingRepository.findRecipientIdsByPhoneBook(mockPhoneBook))
                .thenReturn(existingRecipientIds); // 기존 수신자 ID 목록 반환

        // 벌크 INSERT 후 생성된 매핑들을 모방합니다.
        List<GroupMapping> savedMappings = List.of(
                GroupMapping.builder()
                        .phoneBook(mockPhoneBook)
                        .recipient(mockRecipients.get(0))
                        .build(),
                GroupMapping.builder()
                        .phoneBook(mockPhoneBook)
                        .recipient(mockRecipients.get(1))
                        .build()
        );
        when(groupMappingRepository.findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn(
                eq(phoneBookId), eq(List.of(2, 3))))
                .thenReturn(savedMappings);

        // when
        // 1. 테스트 대상 서비스 메서드를 호출합니다.
        PhoneBookResponse.ModifiedRecipientsDTO result = phoneBookService.addRecipientsToPhoneBook(requestDTO, workspaceId, phoneBookId, userId);

        // then
        // 1. 결과 DTO가 null이 아닌지 확인합니다.
        assertThat(result).isNotNull();
        // 2. 결과 DTO의 주소록 ID가 올바른지 확인합니다.
        assertThat(result.getPhoneBookId()).isEqualTo(phoneBookId);
        // 3. 실제로 추가된 수신자 목록의 크기가 2인지 확인합니다.
        assertThat(result.getRecipientList().size()).isEqualTo(2);
        // 4. 추가된 수신자들의 ID가 2와 3인지 확인합니다.
        assertThat(result.getRecipientList()).extracting("recipientId").containsExactlyInAnyOrder(2, 3);
        // 5. bulkInsertMappings와 findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn가 정확히 1번씩 호출되었는지 검증합니다.
        verify(groupMappingRepository, times(1)).bulkInsertMappings(eq(phoneBookId), eq(List.of(2, 3)), any(LocalDateTime.class));
        verify(groupMappingRepository, times(1)).findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn(eq(phoneBookId), eq(List.of(2, 3)));
    }

    @Test
    @DisplayName("주소록에 수신자 일괄 추가 테스트 - 일부는 중복, 일부는 신규")
    void addRecipientsToPhoneBook_Success_MixDuplicateAndNew_Test() {
        // given
        // 1. 테스트용 ID와 요청 DTO를 정의합니다. (1번은 중복, 2번은 신규)
        Integer workspaceId = 1, phoneBookId = 1, userId = 1;
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(List.of(1, 2));

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook mockPhoneBook = PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("테스트 주소록")
                .workspace(mockWorkspace)
                .build();
        List<Recipient> mockRecipients = List.of(
                Recipient.builder()
                        .recipientId(1)
                        .recipientName("홍길동")
                        .recipientPhoneNumber("010-1111-1111")
                        .workspace(mockWorkspace)
                        .build(),
                Recipient.builder()
                        .recipientId(2)
                        .recipientName("임꺽정")
                        .recipientPhoneNumber("010-1111-2222")
                        .workspace(mockWorkspace)
                        .build()
        );
        List<Integer> existingRecipientIds = List.of(1); // 기존에 1번 수신자가 이미 존재

        // 3. Mock 객체들의 동작을 정의합니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(mockPhoneBook);
        when(recipientValidator.validateAndGetRecipients(workspaceId, requestDTO.getRecipientIds()))
                .thenReturn(mockRecipients);
        when(groupMappingRepository.findRecipientIdsByPhoneBook(mockPhoneBook))
                .thenReturn(existingRecipientIds);

        // 새로 추가될 수신자(ID=2)에 대한 매핑을 모방합니다.
        List<GroupMapping> savedMappings = List.of(
                GroupMapping.builder()
                        .phoneBook(mockPhoneBook)
                        .recipient(mockRecipients.get(1)) // ID=2인 수신자
                        .build()
        );
        when(groupMappingRepository.findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn(
                eq(phoneBookId), eq(List.of(2))))
                .thenReturn(savedMappings);

        // when
        // 1. 테스트 대상 서비스 메서드를 호출합니다.
        PhoneBookResponse.ModifiedRecipientsDTO result = phoneBookService.addRecipientsToPhoneBook(requestDTO, workspaceId, phoneBookId, userId);

        // then
        // 1. 결과 DTO가 null이 아닌지 확인합니다.
        assertThat(result).isNotNull();
        // 2. 실제로 추가된 수신자는 1명(2번)이어야 합니다.
        assertThat(result.getRecipientList().size()).isEqualTo(1);
        // 3. 추가된 수신자의 ID가 2번인지 확인합니다.
        assertThat(result.getRecipientList().getFirst().getRecipientId()).isEqualTo(2);
        // 4. bulkInsertMappings와 findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn가 정확히 1번씩 호출되었는지 검증합니다.
        verify(groupMappingRepository, times(1)).bulkInsertMappings(eq(phoneBookId), eq(List.of(2)), any(LocalDateTime.class));
        verify(groupMappingRepository, times(1)).findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn(eq(phoneBookId), eq(List.of(2)));
    }

    @Test
    @DisplayName("주소록에 수신자 일괄 추가 테스트 - 추가할 신규 수신자가 없는 경우")
    void addRecipientsToPhoneBook_NoNewRecipientsToAdd_Test() {
        // given
        // 1. 테스트용 ID와 요청 DTO를 정의합니다. (요청한 1번 수신자가 이미 존재)
        Integer workspaceId = 1, phoneBookId = 1, userId = 1;
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(List.of(1));

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook mockPhoneBook = PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("테스트 주소록")
                .workspace(mockWorkspace)
                .build();
        List<Recipient> mockRecipients = List.of(Recipient.builder()
                .recipientId(1)
                .recipientName("임꺽정")
                .recipientPhoneNumber("010-1111-2222")
                .workspace(mockWorkspace)
                .build());
        List<Integer> existingRecipientIds = List.of(1);

        // 3. Mock 객체들의 동작을 정의합니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(mockPhoneBook);
        when(recipientValidator.validateAndGetRecipients(workspaceId, requestDTO.getRecipientIds()))
                .thenReturn(mockRecipients);
        when(groupMappingRepository.findRecipientIdsByPhoneBook(mockPhoneBook))
                .thenReturn(existingRecipientIds);

        // when
        // 1. 테스트 대상 서비스 메서드를 호출합니다.
        PhoneBookResponse.ModifiedRecipientsDTO result = phoneBookService.addRecipientsToPhoneBook(requestDTO, workspaceId, phoneBookId, userId);

        // then
        // 1. 결과 DTO가 null이 아닌지 확인합니다.
        assertThat(result).isNotNull();
        // 2. 추가된 수신자 목록이 비어있는지 확인합니다.
        assertThat(result.getRecipientList()).isEmpty();
        // 3. (중요) 신규 추가할 수신자가 없으므로 bulkInsertMappings와 findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn 메서드가 호출되지 않아야 합니다.
        verify(groupMappingRepository, never()).bulkInsertMappings(any(), any(), any());
        verify(groupMappingRepository, never()).findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn(any(), any());
    }

    @Test
    @DisplayName("워크스페이스별 주소록 목록 조회 성공 테스트")
    void readPhoneBooks_Success_Test() {
        // given
        // 1. 테스트에 사용할 ID를 준비합니다.
        Integer workspaceId = 1;
        Integer userId = 1;

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        List<PhoneBook> mockPhoneBookList = List.of(
                PhoneBook.builder()
                        .phoneBookId(1)
                        .phoneBookName("주소록1")
                        .workspace(mockWorkspace)
                        .build(),
                PhoneBook.builder()
                        .phoneBookId(2)
                        .phoneBookName("주소록2")
                        .workspace(mockWorkspace)
                        .build()
        );

        // 3. Mockito 행동 정의
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookRepository.findAllByWorkspace_WorkspaceId(workspaceId))
                .thenReturn(mockPhoneBookList);

        // when
        // 1. 실제 테스트 대상인 서비스 메소드를 호출합니다.
        List<PhoneBookResponse.SimpleDTO> result = phoneBookService.readPhoneBooks(workspaceId, userId);

        // then
        // 1. 반환된 결과가 null이 아닌지 확인합니다.
        assertThat(result).isNotNull();
        // 2. 반환된 주소록 목록의 크기가 2개인지 확인합니다.
        assertThat(result).hasSize(2);
        // 3. 반환된 주소록들의 ID와 이름이 예상과 일치하는지 확인합니다.
        assertThat(result)
                .extracting(PhoneBookResponse.SimpleDTO::getPhoneBookId)
                .containsExactly(1, 2);
        assertThat(result)
                .extracting(PhoneBookResponse.SimpleDTO::getPhoneBookName)
                .containsExactly("주소록1", "주소록2");

        // 4. Validator와 Repository의 메소드가 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
        verify(phoneBookRepository, times(1)).findAllByWorkspace_WorkspaceId(workspaceId);
    }

    @Test
    @DisplayName("워크스페이스별 주소록 목록 조회 실패 테스트 - 권한 없는 워크스페이스")
    void readPhoneBooks_Fail_UnauthorizedWorkspace_Test() {
        // given
        // 1. 테스트에 사용할 ID를 준비합니다.
        Integer unauthorizedWorkspaceId = 999;
        Integer userId = 1;

        // 2. Mockito 행동 정의: workspaceValidator가 예외를 던지도록 설정합니다.
        when(workspaceValidator.validateAndGetWorkspace(unauthorizedWorkspaceId, userId))
                .thenThrow(new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId));

        // when
        // 1. 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.readPhoneBooks(unauthorizedWorkspaceId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId,
                thrown.getMessage());

        // 2. (중요) 로직이 초반에 중단되었으므로, Repository 조회 로직은 절대 호출되면 안됩니다.
        verify(phoneBookRepository, never()).findAllByWorkspace_WorkspaceId(anyInt());
    }

    @Test
    @DisplayName("주소록별 수신자 목록 페이징 조회 성공 테스트")
    void readRecipientsInPhoneBook_Success_Test() {
        // given
        // 1. 테스트에 사용할 ID와 페이징 정보를 준비합니다.
        Integer workspaceId = 1;
        Integer phoneBookId = 1;
        Integer userId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook mockPhoneBook = PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("테스트 주소록")
                .workspace(mockWorkspace)
                .build();

        List<GroupMapping> mockGroupMappings = List.of(
                GroupMapping.builder()
                        .phoneBook(mockPhoneBook)
                        .recipient(Recipient.builder()
                                .recipientId(1)
                                .recipientName("수신자1")
                                .recipientPhoneNumber("010-1111-1111")
                                .workspace(mockWorkspace)
                                .build())
                        .build(),
                GroupMapping.builder()
                        .phoneBook(mockPhoneBook)
                        .recipient(Recipient.builder()
                                .recipientId(2)
                                .recipientName("수신자2")
                                .recipientPhoneNumber("010-2222-2222")
                                .workspace(mockWorkspace)
                                .build())
                        .build()
        );
        Page<GroupMapping> mockGroupMappingPage = new PageImpl<>(mockGroupMappings, pageable, 2);

        // 3. Mockito 행동 정의
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(mockPhoneBook);
        when(groupMappingRepository.findByPhoneBook(mockPhoneBook, pageable))
                .thenReturn(mockGroupMappingPage);

        // when
        // 1. 실제 테스트 대상인 서비스 메소드를 호출합니다.
        Page<RecipientResponse.SimpleDTO> result = phoneBookService.readRecipientsInPhoneBook(workspaceId, phoneBookId, userId, pageable);

        // then
        // 1. 반환된 결과가 null이 아닌지 확인합니다.
        assertThat(result).isNotNull();
        // 2. 페이지의 총 요소 개수가 2개인지 확인합니다.
        assertThat(result.getTotalElements()).isEqualTo(2);
        // 3. 현재 페이지의 요소 개수가 2개인지 확인합니다.
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        // 4. 반환된 수신자들의 ID가 예상과 일치하는지 확인합니다.
        assertThat(result.getContent())
                .extracting(RecipientResponse.SimpleDTO::getRecipientId)
                .containsExactly(1, 2);
        // 5. 반환된 수신자들의 이름이 예상과 일치하는지 확인합니다.
        assertThat(result.getContent())
                .extracting(RecipientResponse.SimpleDTO::getRecipientName)
                .containsExactly("수신자1", "수신자2");

        // 6. Validator와 Repository의 메소드가 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
        verify(phoneBookValidator, times(1)).validateAndGetPhoneBook(workspaceId, phoneBookId);
        verify(groupMappingRepository, times(1)).findByPhoneBook(mockPhoneBook, pageable);
    }

    @Test
    @DisplayName("주소록별 수신자 목록 페이징 조회 실패 테스트 - 존재하지 않는 주소록")
    void readRecipientsInPhoneBook_Fail_PhoneBookNotFound_Test() {
        // given
        // 1. 테스트에 사용할 ID와 페이징 정보를 준비합니다.
        Integer workspaceId = 1;
        Integer nonExistentPhoneBookId = 999;
        Integer userId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        // 2. Mock 객체를 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);

        // 3. Mockito 행동 정의
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, nonExistentPhoneBookId))
                .thenThrow(new IllegalArgumentException("주소록을 찾을 수 없습니다. ID: " + nonExistentPhoneBookId));

        // when
        // 1. 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.readRecipientsInPhoneBook(workspaceId, nonExistentPhoneBookId, userId, pageable));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("주소록을 찾을 수 없습니다. ID: " + nonExistentPhoneBookId,
                thrown.getMessage());

        // 2. (중요) 로직이 초반에 중단되었으므로, GroupMapping 조회 로직은 절대 호출되면 안됩니다.
        verify(groupMappingRepository, never()).findByPhoneBook(any(PhoneBook.class), any(Pageable.class));
    }

    @Test
    @DisplayName("주소록 수정 성공 테스트")
    void updatePhoneBook_Success_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer workspaceId = 1;
        Integer phoneBookId = 1;
        Integer userId = 1;
        PhoneBookRequest.UpdateDTO updateDTO = PhoneBookRequest.UpdateDTO.builder()
                .newPhoneBookName("수정된 주소록명")
                .newPhoneBookMemo("수정된 메모입니다.")
                .build();

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook existingPhoneBook = spy(PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("기존 주소록명")
                .phoneBookMemo("기존 메모")
                .workspace(mockWorkspace)
                .build());

        // 3. Mockito 행동 정의
        //    - workspaceValidator.validateAndGetWorkspace 호출 시, mockWorkspace를 반환하여 권한 검증을 통과시킵니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        //    - phoneBookValidator.validateAndGetPhoneBook 호출 시, existingPhoneBook를 반환합니다.
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(existingPhoneBook);

        // when
        // 1. 실제 테스트 대상인 서비스 메소드를 호출합니다.
        PhoneBookResponse.SimpleDTO result = phoneBookService.updatePhoneBook(updateDTO, workspaceId, phoneBookId, userId);

        // then
        // 1. 반환된 DTO가 null이 아닌지, 그리고 필드 값들이 수정된 데이터와 일치하는지 검증합니다.
        assertNotNull(result);
        assertEquals(phoneBookId, result.getPhoneBookId());
        assertEquals("수정된 주소록명", result.getPhoneBookName());
        assertEquals("수정된 메모입니다.", result.getPhoneBookMemo());

        // 2. 엔티티의 필드가 실제로 업데이트되었는지 검증합니다.
        assertEquals("수정된 주소록명", existingPhoneBook.getPhoneBookName());
        assertEquals("수정된 메모입니다.", existingPhoneBook.getPhoneBookMemo());

        // 3. update() 메서드가 호출되어 updatedAt이 갱신되었는지 검증합니다.
        verify(existingPhoneBook, times(1)).update();

        // 4. Validator들이 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
        verify(phoneBookValidator, times(1)).validateAndGetPhoneBook(workspaceId, phoneBookId);
    }

    @Test
    @DisplayName("주소록 수정 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void updatePhoneBook_Fail_UnauthorizedWorkspace_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer unauthorizedWorkspaceId = 999;
        Integer phoneBookId = 1;
        Integer userId = 1;
        PhoneBookRequest.UpdateDTO updateDTO = PhoneBookRequest.UpdateDTO.builder()
                .newPhoneBookName("수정된 주소록명")
                .newPhoneBookMemo("수정된 메모입니다.")
                .build();

        // 2. Mockito 행동 정의: workspaceValidator가 예외를 던지도록 설정합니다.
        when(workspaceValidator.validateAndGetWorkspace(unauthorizedWorkspaceId, userId))
                .thenThrow(new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId));

        // when
        // 1. 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.updatePhoneBook(updateDTO, unauthorizedWorkspaceId, phoneBookId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId,
                thrown.getMessage());

        // 2. (중요) 로직이 초반에 중단되었으므로, 주소록 검증 로직은 절대 호출되면 안됩니다.
        verify(phoneBookValidator, never()).validateAndGetPhoneBook(anyInt(), anyInt());
    }

    @Test
    @DisplayName("주소록 수정 실패 테스트 - 존재하지 않는 주소록")
    void updatePhoneBook_Fail_PhoneBookNotFound_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer workspaceId = 1;
        Integer nonExistentPhoneBookId = 999;
        Integer userId = 1;
        PhoneBookRequest.UpdateDTO updateDTO = PhoneBookRequest.UpdateDTO.builder()
                .newPhoneBookName("수정된 주소록명")
                .newPhoneBookMemo("수정된 메모입니다.")
                .build();

        // 2. Mock 객체를 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);

        // 3. Mockito 행동 정의
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, nonExistentPhoneBookId))
                .thenThrow(new IllegalArgumentException("주소록을 찾을 수 없습니다. ID: " + nonExistentPhoneBookId));

        // when
        // 1. 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.updatePhoneBook(updateDTO, workspaceId, nonExistentPhoneBookId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("주소록을 찾을 수 없습니다. ID: " + nonExistentPhoneBookId,
                thrown.getMessage());

        // 2. Validator들이 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
        verify(phoneBookValidator, times(1)).validateAndGetPhoneBook(workspaceId, nonExistentPhoneBookId);
    }

    @Test
    @DisplayName("주소록 삭제 성공 테스트")
    void deletePhoneBook_Success_Test() {
        // given
        // 1. 테스트에 사용할 ID를 준비합니다.
        Integer workspaceId = 1;
        Integer phoneBookId = 1;
        Integer userId = 1;

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook existingPhoneBook = spy(PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("삭제할 주소록")
                .phoneBookMemo("삭제 테스트용 메모")
                .workspace(mockWorkspace)
                .build());

        // 3. Mockito 행동 정의
        //    - workspaceValidator.validateAndGetWorkspace 호출 시, mockWorkspace를 반환하여 권한 검증을 통과시킵니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        //    - phoneBookValidator.validateAndGetPhoneBook 호출 시, existingPhoneBook를 반환합니다.
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(existingPhoneBook);
        //    - phoneBookRepository.findByIdIncludingDeleted 호출 시, 소프트 딜리트된 주소록을 반환합니다.
        when(phoneBookRepository.findByIdIncludingDeleted(phoneBookId))
                .thenReturn(java.util.Optional.of(existingPhoneBook));

        // when
        // 1. 실제 테스트 대상인 서비스 메소드를 호출합니다.
        PhoneBookResponse.SimpleDTO result = phoneBookService.deletePhoneBook(workspaceId, phoneBookId, userId);

        // then
        // 1. 반환된 DTO가 null이 아닌지, 그리고 필드 값들이 예상과 일치하는지 검증합니다.
        assertNotNull(result);
        assertEquals(phoneBookId, result.getPhoneBookId());
        assertEquals("삭제할 주소록", result.getPhoneBookName());
        assertEquals("삭제 테스트용 메모", result.getPhoneBookMemo());

        // 2. 엔티티의 softDelete() 메서드가 호출되었는지 검증합니다.
        verify(existingPhoneBook, times(1)).softDelete();

        // 3. EntityManager flush/clear가 호출되었는지 검증합니다.
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();

        // 4. 소프트 딜리트된 주소록 재조회가 호출되었는지 검증합니다.
        verify(phoneBookRepository, times(1)).findByIdIncludingDeleted(phoneBookId);

        // 5. Validator들이 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
        verify(phoneBookValidator, times(1)).validateAndGetPhoneBook(workspaceId, phoneBookId);
    }

    @Test
    @DisplayName("주소록 삭제 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void deletePhoneBook_Fail_UnauthorizedWorkspace_Test() {
        // given
        // 1. 테스트에 사용할 ID를 준비합니다.
        Integer unauthorizedWorkspaceId = 999;
        Integer phoneBookId = 1;
        Integer userId = 1;

        // 2. Mockito 행동 정의: workspaceValidator가 예외를 던지도록 설정합니다.
        when(workspaceValidator.validateAndGetWorkspace(unauthorizedWorkspaceId, userId))
                .thenThrow(new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId));

        // when
        // 1. 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.deletePhoneBook(unauthorizedWorkspaceId, phoneBookId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId,
                thrown.getMessage());

        // 2. (중요) 로직이 초반에 중단되었으므로, 주소록 검증 로직은 절대 호출되면 안됩니다.
        verify(phoneBookValidator, never()).validateAndGetPhoneBook(anyInt(), anyInt());
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
        verify(phoneBookRepository, never()).findByIdIncludingDeleted(anyInt());
    }

    @Test
    @DisplayName("주소록 삭제 실패 테스트 - 존재하지 않는 주소록")
    void deletePhoneBook_Fail_PhoneBookNotFound_Test() {
        // given
        // 1. 테스트에 사용할 ID를 준비합니다.
        Integer workspaceId = 1;
        Integer nonExistentPhoneBookId = 999;
        Integer userId = 1;

        // 2. Mock 객체를 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);

        // 3. Mockito 행동 정의
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, nonExistentPhoneBookId))
                .thenThrow(new IllegalArgumentException("주소록을 찾을 수 없습니다. ID: " + nonExistentPhoneBookId));

        // when
        // 1. 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.deletePhoneBook(workspaceId, nonExistentPhoneBookId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("주소록을 찾을 수 없습니다. ID: " + nonExistentPhoneBookId,
                thrown.getMessage());

        // 2. Validator들이 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
        verify(phoneBookValidator, times(1)).validateAndGetPhoneBook(workspaceId, nonExistentPhoneBookId);

        // 3. (중요) 삭제 로직은 호출되면 안됩니다.
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
        verify(phoneBookRepository, never()).findByIdIncludingDeleted(anyInt());
    }

    @Test
    @DisplayName("주소록 삭제 실패 테스트 - 소프트 딜리트 후 재조회 실패")
    void deletePhoneBook_Fail_RetrievalAfterSoftDelete_Test() {
        // given
        // 1. 테스트에 사용할 ID를 준비합니다.
        Integer workspaceId = 1;
        Integer phoneBookId = 1;
        Integer userId = 1;

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook existingPhoneBook = spy(PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("삭제할 주소록")
                .phoneBookMemo("삭제 테스트용 메모")
                .workspace(mockWorkspace)
                .build());

        // 3. Mockito 행동 정의
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(existingPhoneBook);
        // 소프트 딜리트 후 재조회가 실패하는 상황을 시뮬레이트합니다.
        when(phoneBookRepository.findByIdIncludingDeleted(phoneBookId))
                .thenReturn(java.util.Optional.empty());

        // when
        // 1. 서비스 메소드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalStateException.class, () ->
                phoneBookService.deletePhoneBook(workspaceId, phoneBookId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("소프트 딜리트 처리된 주소록을 재조회하는 데 실패했습니다. ID: " + phoneBookId,
                thrown.getMessage());

        // 2. 소프트 딜리트는 실행되었는지 검증합니다.
        verify(existingPhoneBook, times(1)).softDelete();
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
        verify(phoneBookRepository, times(1)).findByIdIncludingDeleted(phoneBookId);
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 성공 테스트")
    void deleteRecipientsFromPhoneBook_Success_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer workspaceId = 1;
        Integer phoneBookId = 1;
        Integer userId = 1;
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(List.of(1, 2));

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook mockPhoneBook = PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("테스트 주소록")
                .workspace(mockWorkspace)
                .build();

        Recipient recipient1 = Recipient.builder()
                .recipientId(1)
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1111-1111")
                .workspace(mockWorkspace)
                .build();
        Recipient recipient2 = Recipient.builder()
                .recipientId(2)
                .recipientName("임꺽정")
                .recipientPhoneNumber("010-2222-2222")
                .workspace(mockWorkspace)
                .build();

        GroupMapping mapping1 = GroupMapping.builder()
                .groupMappingId(1)
                .phoneBook(mockPhoneBook)
                .recipient(recipient1)
                .build();
        GroupMapping mapping2 = GroupMapping.builder()
                .groupMappingId(2)
                .phoneBook(mockPhoneBook)
                .recipient(recipient2)
                .build();

        List<GroupMapping> mappingsToDelete = List.of(mapping1, mapping2);

        // 소프트 딜리트된 매핑들 (deletedAt과 updatedAt이 설정된 상태)
        GroupMapping deletedMapping1 = spy(GroupMapping.builder()
                .groupMappingId(1)
                .phoneBook(mockPhoneBook)
                .recipient(recipient1)
                .build());
        GroupMapping deletedMapping2 = spy(GroupMapping.builder()
                .groupMappingId(2)
                .phoneBook(mockPhoneBook)
                .recipient(recipient2)
                .build());
        List<GroupMapping> deletedMappings = List.of(deletedMapping1, deletedMapping2);

        // 3. Mock 객체들의 동작을 정의합니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(mockPhoneBook);
        when(recipientValidator.validateAndGetRecipients(workspaceId, requestDTO.getRecipientIds()))
                .thenReturn(List.of(recipient1, recipient2)); // 유효한 수신자 반환
        when(groupMappingRepository.findAllByPhoneBookAndRecipient_RecipientIdIn(mockPhoneBook, List.of(1, 2)))
                .thenReturn(mappingsToDelete);
        when(groupMappingRepository.findAllByIdIncludingDeleted(List.of(mapping1.getGroupMappingId(), mapping2.getGroupMappingId())))
                .thenReturn(deletedMappings);

        // when
        // 1. 테스트 대상 서비스 메서드를 호출합니다.
        PhoneBookResponse.ModifiedRecipientsDTO result = phoneBookService.deleteRecipientsFromPhoneBook(requestDTO, workspaceId, phoneBookId, userId);

        // then
        // 1. 결과 DTO가 null이 아닌지 확인합니다.
        assertThat(result).isNotNull();
        // 2. 결과 DTO의 주소록 ID가 올바른지 확인합니다.
        assertThat(result.getPhoneBookId()).isEqualTo(phoneBookId);
        // 3. 삭제된 수신자 목록의 크기가 2인지 확인합니다.
        assertThat(result.getRecipientList().size()).isEqualTo(2);
        // 4. 삭제된 수신자들의 ID가 1과 2인지 확인합니다.
        assertThat(result.getRecipientList()).extracting("recipientId").containsExactlyInAnyOrder(1, 2);

        // 5. groupMappingRepository.softDeleteAllInBatch가 정확히 1번 호출되었는지 검증합니다.
        verify(groupMappingRepository, times(1)).softDeleteAllInBatch(eq(mappingsToDelete), any(LocalDateTime.class));
        // 6. 소프트 딜리트된 매핑들을 다시 조회했는지 검증합니다.
        verify(groupMappingRepository, times(1)).findAllByIdIncludingDeleted(List.of(mapping1.getGroupMappingId(), mapping2.getGroupMappingId()));
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 테스트 - 삭제할 매핑이 없는 경우")
    void deleteRecipientsFromPhoneBook_NoMappingsToDelete_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer workspaceId = 1;
        Integer phoneBookId = 1;
        Integer userId = 1;
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(List.of(1, 2)); // 유효한 수신자 ID지만 주소록에 매핑되지 않음

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook mockPhoneBook = PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("테스트 주소록")
                .workspace(mockWorkspace)
                .build();

        // 유효한 수신자들 (워크스페이스에는 존재하지만 주소록에는 매핑되지 않음)
        List<Recipient> validRecipients = List.of(
                Recipient.builder()
                        .recipientId(1)
                        .recipientName("홍길동")
                        .recipientPhoneNumber("010-1111-1111")
                        .workspace(mockWorkspace)
                        .build(),
                Recipient.builder()
                        .recipientId(2)
                        .recipientName("임꺽정")
                        .recipientPhoneNumber("010-2222-2222")
                        .workspace(mockWorkspace)
                        .build()
        );

        // 3. Mock 객체들의 동작을 정의합니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(mockPhoneBook);
        when(recipientValidator.validateAndGetRecipients(workspaceId, requestDTO.getRecipientIds()))
                .thenReturn(validRecipients); // 유효한 수신자 반환
        when(groupMappingRepository.findAllByPhoneBookAndRecipient_RecipientIdIn(mockPhoneBook, List.of(1, 2)))
                .thenReturn(List.of()); // 삭제할 매핑이 없음

        // when
        // 1. 테스트 대상 서비스 메서드를 호출합니다.
        PhoneBookResponse.ModifiedRecipientsDTO result = phoneBookService.deleteRecipientsFromPhoneBook(requestDTO, workspaceId, phoneBookId, userId);

        // then
        // 1. 결과 DTO가 null이 아닌지 확인합니다.
        assertThat(result).isNotNull();
        // 2. 결과 DTO의 주소록 ID가 올바른지 확인합니다.
        assertThat(result.getPhoneBookId()).isEqualTo(phoneBookId);
        // 3. 삭제된 수신자 목록이 비어있는지 확인합니다.
        assertThat(result.getRecipientList()).isEmpty();

        // 4. 삭제할 매핑이 없으므로 softDeleteAllInBatch가 호출되지 않았는지 검증합니다.
        verify(groupMappingRepository, never()).softDeleteAllInBatch(anyList(), any(LocalDateTime.class));
        // 5. 재조회도 호출되지 않았는지 검증합니다.
        verify(groupMappingRepository, never()).findAllByIdIncludingDeleted(anyList());
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 실패 테스트 - 존재하지 않거나 권한이 없는 워크스페이스")
    void deleteRecipientsFromPhoneBook_Fail_UnauthorizedWorkspace_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer unauthorizedWorkspaceId = 999;
        Integer phoneBookId = 1;
        Integer userId = 1;
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(List.of(1, 2));

        // 2. Mockito 행동 정의: workspaceValidator가 예외를 던지도록 설정합니다.
        when(workspaceValidator.validateAndGetWorkspace(unauthorizedWorkspaceId, userId))
                .thenThrow(new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId));

        // when
        // 1. 서비스 메서드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.deleteRecipientsFromPhoneBook(requestDTO, unauthorizedWorkspaceId, phoneBookId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + unauthorizedWorkspaceId,
                thrown.getMessage());

        // 2. (중요) 로직이 초반에 중단되었으므로, 다른 검증 로직들은 절대 호출되면 안됩니다.
        verify(phoneBookValidator, never()).validateAndGetPhoneBook(anyInt(), anyInt());
        verify(groupMappingRepository, never()).findAllByPhoneBookAndRecipient_RecipientIdIn(any(PhoneBook.class), anyList());
        verify(groupMappingRepository, never()).softDeleteAllInBatch(anyList(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 실패 테스트 - 존재하지 않는 주소록")
    void deleteRecipientsFromPhoneBook_Fail_PhoneBookNotFound_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer workspaceId = 1;
        Integer nonExistentPhoneBookId = 999;
        Integer userId = 1;
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(List.of(1, 2));

        // 2. Mock 객체를 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);

        // 3. Mockito 행동 정의
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, nonExistentPhoneBookId))
                .thenThrow(new IllegalArgumentException("주소록을 찾을 수 없습니다. ID: " + nonExistentPhoneBookId));

        // when
        // 1. 서비스 메서드를 호출했을 때 특정 예외가 발생하는지 검증합니다.
        Throwable thrown = assertThrows(IllegalArgumentException.class, () ->
                phoneBookService.deleteRecipientsFromPhoneBook(requestDTO, workspaceId, nonExistentPhoneBookId, userId));

        // then
        // 1. 발생한 예외의 메시지가 예상과 정확히 일치하는지 확인합니다.
        assertEquals("주소록을 찾을 수 없습니다. ID: " + nonExistentPhoneBookId,
                thrown.getMessage());

        // 2. Validator들이 각각 정확히 1번씩 호출되었는지 검증합니다.
        verify(workspaceValidator, times(1)).validateAndGetWorkspace(workspaceId, userId);
        verify(phoneBookValidator, times(1)).validateAndGetPhoneBook(workspaceId, nonExistentPhoneBookId);

        // 3. (중요) 삭제 로직은 호출되면 안됩니다.
        verify(groupMappingRepository, never()).findAllByPhoneBookAndRecipient_RecipientIdIn(any(PhoneBook.class), anyList());
        verify(groupMappingRepository, never()).softDeleteAllInBatch(anyList(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 테스트 - 소프트 딜리트 후 재조회 시 빈 결과")
    void deleteRecipientsFromPhoneBook_EmptyResultAfterSoftDelete_Test() {
        // given
        // 1. 테스트에 사용할 ID와 요청 DTO를 준비합니다.
        Integer workspaceId = 1;
        Integer phoneBookId = 1;
        Integer userId = 1;
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(List.of(1, 2));

        // 2. Mock 객체들을 준비합니다.
        Workspace mockWorkspace = mock(Workspace.class);
        PhoneBook mockPhoneBook = PhoneBook.builder()
                .phoneBookId(phoneBookId)
                .phoneBookName("테스트 주소록")
                .workspace(mockWorkspace)
                .build();

        GroupMapping mapping1 = GroupMapping.builder()
                .groupMappingId(1)
                .phoneBook(mockPhoneBook)
                .recipient(Recipient.builder()
                        .recipientId(1)
                        .recipientName("홍길동")
                        .recipientPhoneNumber("010-1111-1111")
                        .workspace(mockWorkspace)
                        .build())
                .build();
        List<GroupMapping> mappingsToDelete = List.of(mapping1);

        Recipient validRecipient1 = Recipient.builder()
                .recipientId(1)
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1111-1111")
                .workspace(mockWorkspace)
                .build();
        Recipient validRecipient2 = Recipient.builder()
                .recipientId(2)
                .recipientName("임꺽정")
                .recipientPhoneNumber("010-2222-2222")
                .workspace(mockWorkspace)
                .build();

        // 3. Mock 객체들의 동작을 정의합니다.
        when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                .thenReturn(mockWorkspace);
        when(phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId))
                .thenReturn(mockPhoneBook);
        when(recipientValidator.validateAndGetRecipients(workspaceId, requestDTO.getRecipientIds()))
                .thenReturn(List.of(validRecipient1, validRecipient2)); // 유효한 수신자 반환
        when(groupMappingRepository.findAllByPhoneBookAndRecipient_RecipientIdIn(mockPhoneBook, List.of(1, 2)))
                .thenReturn(mappingsToDelete);
        // 소프트 딜리트 후 재조회 시 빈 리스트 반환 (실제로는 발생하지 않지만 테스트용)
        when(groupMappingRepository.findAllByIdIncludingDeleted(List.of(mapping1.getGroupMappingId())))
                .thenReturn(List.of()); // 빈 리스트 반환

        // when
        // 1. 서비스 메서드를 호출합니다.
        PhoneBookResponse.ModifiedRecipientsDTO result = phoneBookService.deleteRecipientsFromPhoneBook(requestDTO, workspaceId, phoneBookId, userId);

        // then
        // 1. 결과 DTO가 null이 아닌지 확인합니다.
        assertThat(result).isNotNull();
        // 2. 결과 DTO의 주소록 ID가 올바른지 확인합니다.
        assertThat(result.getPhoneBookId()).isEqualTo(phoneBookId);
        // 3. 재조회 결과가 빈 리스트이므로 수신자 목록도 빈 것으로 반환됩니다.
        assertThat(result.getRecipientList()).isEmpty();

        // 4. 소프트 딜리트는 실행되었는지 검증합니다.
        verify(groupMappingRepository, times(1)).softDeleteAllInBatch(eq(mappingsToDelete), any(LocalDateTime.class));
        verify(groupMappingRepository, times(1)).findAllByIdIncludingDeleted(List.of(mapping1.getGroupMappingId()));
    }
}
