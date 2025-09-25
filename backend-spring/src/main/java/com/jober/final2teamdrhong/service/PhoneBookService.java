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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주소록(PhoneBook) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhoneBookService {

    private final PhoneBookRepository phoneBookRepository;
    private final GroupMappingRepository groupMappingRepository;
    private final PhoneBookValidator phoneBookValidator;
    private final WorkspaceValidator workspaceValidator;
    private final RecipientValidator recipientValidator;
    private final EntityManager entityManager;

    /**
     * 특정 워크스페이스에 새로운 주소록을 생성합니다.
     *
     * @param createDTO   주소록 생성을 위한 요청 데이터
     * @param workspaceId 주소록을 추가할 워크스페이스의 ID
     * @param userId      요청을 보낸 사용자의 ID (인가에 사용)
     * @return 생성된 주소록의 정보({@link PhoneBookResponse.SimpleDTO})
     * @throws IllegalArgumentException 해당 워크스페이스가 존재하지 않거나, 사용자가 접근 권한이 없을 경우 발생
     */
    @Transactional
    public PhoneBookResponse.SimpleDTO createPhoneBook(PhoneBookRequest.CreateDTO createDTO, Integer workspaceId, Integer userId) {
        // 1. 인가(Authorization): 요청한 사용자가 워크스페이스에 접근 권한이 있는지 확인합니다.
        Workspace workspace = workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        // 2. 엔티티 생성: DTO의 데이터를 기반으로 PhoneBook 엔티티를 생성합니다.
        PhoneBook phoneBook = PhoneBook.builder()
                .phoneBookName(createDTO.getPhoneBookName())
                .phoneBookMemo(createDTO.getPhoneBookMemo())
                .workspace(workspace)
                .build();

        // 3. 엔티티 저장 및 DTO 변환 후 반환
        PhoneBook createdPhoneBook = phoneBookRepository.save(phoneBook);

        return new PhoneBookResponse.SimpleDTO(createdPhoneBook);
    }

    /**
     * 주소록에 다수의 수신자를 일괄 추가합니다.
     * <p>
     * 전체 로직은 하나의 트랜잭션으로 처리됩니다:
     * <ol>
     *     <li>요청된 워크스페이스, 주소록, 수신자 ID 목록의 유효성을 검증합니다.</li>
     *     <li>요청된 수신자 중 이미 주소록에 존재하는 멤버를 필터링하여 중복 추가를 방지합니다.</li>
     *     <li>실제로 추가할 신규 수신자가 없는 경우, 빈 목록을 포함한 DTO를 반환합니다.</li>
     *     <li>서울 시간대로 통일된 타임스탬프를 생성하고, 네이티브 쿼리를 사용하여
     *         신규 수신자들을 주소록에 일괄 추가합니다 (Bulk Insert).</li>
     *     <li>벌크 INSERT 후 실제 DB에 저장된 GroupMapping 엔티티들을 재조회하여
     *         {@link PhoneBookResponse.ModifiedRecipientsDTO#ofAddition(PhoneBook, List)}
     *         팩토리 메소드를 호출하여 결과 DTO를 생성하고 반환합니다.</li>
     * </ol>
     *
     * @param recipientIdListDTO 추가할 수신자 ID 목록을 담은 DTO
     * @param workspaceId      주소록이 속한 워크스페이스의 ID
     * @param phoneBookId      수신자를 추가할 주소록의 ID
     * @param userId           요청을 보낸 사용자의 ID (인가에 사용)
     * @return '추가' 이벤트의 결과로 생성된 {@link PhoneBookResponse.ModifiedRecipientsDTO}.
     *         실제로 추가된 수신자 목록과 DB에 기록된 작업 시간을 포함합니다.
     * @throws IllegalArgumentException 유효하지 않은 ID(워크스페이스, 주소록, 수신자)로 요청했을 경우 발생
     */
    @Transactional
    public PhoneBookResponse.ModifiedRecipientsDTO addRecipientsToPhoneBook(PhoneBookRequest.RecipientIdListDTO recipientIdListDTO, Integer workspaceId, Integer phoneBookId, Integer userId) {
        // 1. 폰북과 워크스페이스 권한 검증
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);
        PhoneBook phoneBook = phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId);
        List<Recipient> allRequestedRecipients = recipientValidator.validateAndGetRecipients(workspaceId, recipientIdListDTO.getRecipientIds());

        // 2. 중복 방지를 위해 이미 존재하는 수신자 필터링
        List<Integer> existingRecipientIds = groupMappingRepository.findRecipientIdsByPhoneBook(phoneBook);

        // 2-1. 요청된 수신자 중, 실제로 새로 추가해야 할 수신자만 필터링합니다.
        List<Recipient> recipientsToActuallyAdd = allRequestedRecipients.stream()
                .filter(recipient -> !existingRecipientIds.contains(recipient.getRecipientId()))
                .toList();

        // 3. 새로 추가할 수신자가 없는 경우, 빈 목록으로 응답
        if (recipientsToActuallyAdd.isEmpty()) {
            // ofAddition 팩토리 메소드에 빈 GroupMapping 리스트를 전달하여
            // '추가된 멤버 없음'을 나타내는 DTO를 생성합니다.
            return PhoneBookResponse.ModifiedRecipientsDTO.ofAddition(phoneBook, new ArrayList<>());
        }

        // 4. 서울 시간대로 생성 시간을 설정
        LocalDateTime creationTimestamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();

        // 5. 추가할 수신자 ID 목록 추출
        List<Integer> recipientIdsToAdd = recipientsToActuallyAdd.stream()
                .map(Recipient::getRecipientId)
                .toList();

        // 6. 벌크 INSERT 실행 (단일 쿼리로 모든 매핑 생성, 동일한 시간 적용)
        groupMappingRepository.bulkInsertMappings(phoneBook.getPhoneBookId(), recipientIdsToAdd, creationTimestamp);

        // 7. 벌크 INSERT 후 실제 DB에서 생성된 매핑들을 재조회
        List<GroupMapping> savedMappings =
                groupMappingRepository.findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn(phoneBook.getPhoneBookId(), recipientIdsToAdd);

        // 8. '추가'용 팩토리 메소드를 호출하여 최종 DTO를 반환합니다.
        return PhoneBookResponse.ModifiedRecipientsDTO.ofAddition(phoneBook, savedMappings);
    }

    /**
     * 특정 워크스페이스에 속한 모든 주소록 목록을 조회합니다.
     *
     * @param workspaceId 주소록을 조회할 워크스페이스의 ID
     * @param userId      요청을 보낸 사용자의 ID (인가에 사용)
     * @return 해당 워크스페이스의 모든 주소록 정보 목록(List<{@link PhoneBookResponse.SimpleDTO}>)
     * @throws IllegalArgumentException 해당 워크스페이스가 존재하지 않거나, 사용자가 접근 권한이 없을 경우 발생
     */
    public List<PhoneBookResponse.SimpleDTO> readPhoneBooks(Integer workspaceId, Integer userId) {
        // 1. 인가: 사용자가 워크스페이스에 접근 권한이 있는지 검증합니다.
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        // 2. 데이터 조회: Repository를 통해 해당 워크스페이스의 모든 주소록을 조회합니다.
        List<PhoneBook> phoneBookList = phoneBookRepository.findAllByWorkspace_WorkspaceId(workspaceId);

        // 3. DTO 변환: 조회된 엔티티 목록을 DTO 목록으로 변환하여 반환합니다.
        return phoneBookList.stream()
                .map(PhoneBookResponse.SimpleDTO::new)
                .toList();
    }

    /**
     * 특정 주소록에 포함된 수신자 목록을 페이징하여 조회합니다.
     * <p>
     * 수신자의 생성 시간(createdAt) 내림차순으로 정렬되어 반환됩니다.
     * 최신에 생성된 수신자가 목록의 상단에 표시됩니다.
     *
     * @param workspaceId 주소록이 속한 워크스페이스의 ID
     * @param phoneBookId 수신자 목록을 조회할 주소록의 ID
     * @param userId      요청을 보낸 사용자의 ID (인가에 사용)
     * @param pageable    페이징 및 정렬 정보 (정렬 조건은 무시되고 수신자 생성 시간 기준으로 정렬됨)
     * @return 페이징된 수신자 정보 목록(Page<{@link RecipientResponse.SimpleDTO}>)
     * @throws IllegalArgumentException 유효하지 않은 ID(워크스페이스, 주소록)로 요청했을 경우 발생
     */
    public Page<RecipientResponse.SimpleDTO> readRecipientsInPhoneBook(Integer workspaceId, Integer phoneBookId, Integer userId, Pageable pageable) {
        // 1. 인가: 사용자가 워크스페이스와 주소록에 접근 권한이 있는지 검증
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);
        PhoneBook phoneBook = phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId);

        // 2. Repository를 통해 해당 주소록의 GroupMapping 목록을 페이징 조회합니다. (Recipient의 createdAt 내림차순 정렬)
        Page<GroupMapping> recipientsInPhoneBookPage = groupMappingRepository.findByPhoneBookOrderByRecipient_CreatedAtDescRecipient_RecipientIdDesc(phoneBook, pageable);

        // 3. GroupMapping에서 Recipient를 추출하여 RecipientResponse.SimpleDTO로 변환한 후 Page 형태로 반환합니다.
        return recipientsInPhoneBookPage
                .map(groupMapping -> new RecipientResponse.SimpleDTO(groupMapping.getRecipient()));
    }

    /**
     * 특정 주소록의 정보를 수정합니다.
     *
     * @param updateDTO   주소록 수정을 위한 요청 데이터
     * @param workspaceId 주소록이 속한 워크스페이스의 ID
     * @param phoneBookId 수정할 주소록의 ID
     * @param userId      요청을 보낸 사용자의 ID (인가에 사용)
     * @return 수정된 주소록의 정보({@link PhoneBookResponse.SimpleDTO})
     * @throws IllegalArgumentException 유효하지 않은 ID(워크스페이스, 주소록)로 요청했을 경우 발생
     */
    @Transactional
    public PhoneBookResponse.SimpleDTO updatePhoneBook(PhoneBookRequest.UpdateDTO updateDTO, Integer workspaceId, Integer phoneBookId, Integer userId) {
        // 1. 인가: 사용자가 워크스페이스와 주소록에 접근 권한이 있는지 검증
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        // 2. 주소록 조회 (워크스페이스 소속인지 함께 검증)
        PhoneBook existingPhoneBook = phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId);

        // 3. 정보 업데이트
        existingPhoneBook.setPhoneBookName(updateDTO.getNewPhoneBookName());
        existingPhoneBook.setPhoneBookMemo(updateDTO.getNewPhoneBookMemo());
        existingPhoneBook.update();

        return new PhoneBookResponse.SimpleDTO(existingPhoneBook);
    }

    /**
     * 특정 주소록을 소프트 딜리트 처리합니다.
     * <p>
     * 이 메서드는 {@link PhoneBook} 엔티티에 적용된 {@code @SQLRestriction("is_deleted = false")}로 인해
     * 일반적인 조회 메서드로는 소프트 딜리트된 엔티티를 재조회할 수 없는 문제를 해결합니다.
     * 정확한 삭제 시간을 응답으로 반환하기 위해 다음의 과정을 거칩니다:
     * <ol>
     *     <li>엔티티의 상태를 변경합니다 (Dirty Checking 대상).</li>
     *     <li>{@code entityManager.flush()}를 호출하여 변경사항을 DB에 즉시 동기화합니다.</li>
     *     <li>{@code entityManager.clear()}를 호출하여 영속성 컨텍스트를 비웁니다.</li>
     *     <li>{@code @SQLRestriction}을 우회하는 네이티브 쿼리 메서드
     *         ({@link PhoneBookRepository#findByIdIncludingDeleted(Integer)})를 사용하여,
     *         DB에 실제로 기록된 최종 상태를 다시 조회합니다.</li>
     *     <li>재조회된 엔티티를 DTO로 변환하여 반환함으로써, 응답 시간과 DB 시간의 일관성을 보장합니다.</li>
     * </ol>
     *
     * @param workspaceId 주소록이 속한 워크스페이스의 ID
     * @param phoneBookId 소프트 딜리트할 주소록의 ID
     * @param userId      요청을 보낸 사용자의 ID (인가에 사용)
     * @return 소프트 딜리트 처리된 주소록의 정보({@link PhoneBookResponse.SimpleDTO})
     * @throws IllegalArgumentException 유효하지 않은 ID(워크스페이스, 주소록)로 요청했을 경우 또는
     *         소프트 딜리트 후 엔티티를 재조회하는 과정에서 문제가 발생했을 경우 발생
     */
    @Transactional
    public PhoneBookResponse.SimpleDTO deletePhoneBook(Integer workspaceId, Integer phoneBookId, Integer userId) {
        // 1. 인가: 사용자가 워크스페이스와 주소록에 접근 권한이 있는지 검증
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        // 2. 주소록 조회 (워크스페이스 소속인지 함께 검증)
        PhoneBook existingPhoneBook = phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId);

        // 3. 소프트 딜리트 처리
        existingPhoneBook.softDelete();

        // 4. 즉시 DB에 반영, 및 Hibernate 1차 캐시 비우기 후 변경된 DB를 반환해야 정확한 시간이 응답으로 나옴
        entityManager.flush();
        entityManager.clear();

        // 5. @SQLRestriction을 우회하는 네이티브 쿼리로 재조회하여 시간 동기화
        PhoneBook deletedPhoneBook = phoneBookRepository.findByIdIncludingDeleted(phoneBookId)
                .orElseThrow(() -> new IllegalStateException("소프트 딜리트 처리된 주소록을 재조회하는 데 실패했습니다. ID: " + phoneBookId));

        return new PhoneBookResponse.SimpleDTO(deletedPhoneBook);
    }

    /**
     * 주소록에서 다수의 수신자를 일괄 소프트 딜리트 처리합니다.
     * <p>
     * 전체 로직은 하나의 트랜잭션으로 처리되며, 성능과 데이터 정확성을 모두 보장합니다:
     * <ol>
     *     <li>요청된 워크스페이스, 주소록, 수신자 ID 목록의 유효성을 검증합니다.</li>
     *     <li>요청된 수신자 중 실제로 해당 주소록에 매핑된 {@link GroupMapping} 엔티티만 조회합니다.</li>
     *     <li>삭제할 매핑이 없는 경우, 빈 목록을 포함한 DTO를 즉시 반환합니다.</li>
     *     <li>조회된 매핑들을 대상으로 JPQL을 이용한 Bulk Update를 실행하여,
     *         단일 쿼리로 모든 대상을 효율적으로 소프트 딜리트 처리합니다.</li>
     *     <li>Bulk 연산은 영속성 컨텍스트를 우회하므로, DB와 메모리 간의 데이터 정합성을 맞추기 위해
     *         방금 삭제 처리된 {@link GroupMapping} 목록을 DB에서 재조회합니다.</li>
     *     <li>최종적으로, 재조회된 최신 데이터를 기반으로
     *         {@link PhoneBookResponse.ModifiedRecipientsDTO#ofDeletion(PhoneBook, List)}
     *         팩토리 메소드를 호출하여 결과 DTO를 생성하고 반환합니다.</li>
     * </ol>
     *
     * @param recipientIdListDTO 삭제할 수신자 ID 목록을 담은 DTO
     * @param workspaceId      주소록이 속한 워크스페이스의 ID
     * @param phoneBookId      수신자를 삭제할 주소록의 ID
     * @param userId           요청을 보낸 사용자의 ID (인가에 사용)
     * @return '삭제' 이벤트의 결과로 생성된 {@link PhoneBookResponse.ModifiedRecipientsDTO}.
     *         실제로 삭제 처리된 수신자 목록과 DB에 기록된 작업 시간을 포함합니다.
     * @throws IllegalArgumentException 유효하지 않은 ID(워크스페이스, 주소록, 수신자)로 요청했을 경우 발생
     */
    @Transactional
    public PhoneBookResponse.ModifiedRecipientsDTO deleteRecipientsFromPhoneBook(PhoneBookRequest.RecipientIdListDTO recipientIdListDTO, Integer workspaceId, Integer phoneBookId, Integer userId) {
        // 1. 폰북과 워크스페이스 권한 검증
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);
        PhoneBook phoneBook = phoneBookValidator.validateAndGetPhoneBook(workspaceId, phoneBookId);
        List<Recipient> allRequestedRecipients = recipientValidator.validateAndGetRecipients(workspaceId, recipientIdListDTO.getRecipientIds());

        // 2. 삭제할 대상이 될 GroupMapping 엔티티들을 DB에서 조회합니다.
        List<Integer> existingRecipientIds = allRequestedRecipients.stream()
                .map(Recipient::getRecipientId)
                .toList();
        List<GroupMapping> recipientsToActuallyDelete = groupMappingRepository.findAllByPhoneBookAndRecipient_RecipientIdIn(phoneBook, existingRecipientIds);

        // 3. 실제로 삭제할 매핑이 없는 경우, 즉시 빈 결과를 반환합니다.
        if (recipientsToActuallyDelete.isEmpty()) {
            return PhoneBookResponse.ModifiedRecipientsDTO.ofDeletion(phoneBook, new ArrayList<>());
        }

        // 4. "서울 시간"을 명시적으로 생성합니다.
        LocalDateTime deletionTimestamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();

        // 5. Bulk Update 실행 시, 생성한 시간을 파라미터로 전달합니다.
        groupMappingRepository.softDeleteAllInBatch(recipientsToActuallyDelete, deletionTimestamp);

        // 6. 재조회 로직은 그대로 유지하여 DB에 반영된 최신 상태를 가져옵니다.
        List<Integer> mappingIds = recipientsToActuallyDelete.stream()
                .map(GroupMapping::getGroupMappingId)
                .toList();
        List<GroupMapping> deletedMappings =
                groupMappingRepository.findAllByIdIncludingDeleted(mappingIds);

        // 7. 재조회한 최신 데이터로 DTO를 생성하고 반환합니다.
        return PhoneBookResponse.ModifiedRecipientsDTO.ofDeletion(phoneBook, deletedMappings);
    }
}
