package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GroupMappingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GroupMappingRepository groupMappingRepository;

    private PhoneBook testPhoneBook;
    private PhoneBook anotherPhoneBook;
    private Recipient recipient1;
    private Recipient recipient2;
    private Recipient recipient3;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .userName("user")
                .userEmail("user@test.com")
                .build();
        entityManager.persist(user);

        Workspace workspace = Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("테스트대표")
                .representerPhoneNumber("010-0000-0000")
                .companyName("테스트회사")
                .user(user)
                .build();
        entityManager.persist(workspace);

        testPhoneBook = PhoneBook.builder()
                .phoneBookName("test-phonebook")
                .workspace(workspace)
                .build();
        entityManager.persist(testPhoneBook);

        anotherPhoneBook = PhoneBook.builder()
                .phoneBookName("another-phonebook")
                .workspace(workspace)
                .build();
        entityManager.persist(anotherPhoneBook);

        recipient1 = Recipient.builder()
                .recipientName("recipient1")
                .recipientPhoneNumber("010-1111-1111")
                .workspace(workspace)
                .build();
        entityManager.persist(recipient1);

        recipient2 = Recipient.builder()
                .recipientName("recipient2")
                .recipientPhoneNumber("010-1111-1111")
                .workspace(workspace)
                .build();
        entityManager.persist(recipient2);

        recipient3 = Recipient.builder()
                .recipientName("recipient3")
                .recipientPhoneNumber("010-1111-1111")
                .workspace(workspace)
                .build();
        entityManager.persist(recipient3);

        // testPhoneBook에는 recipient1, recipient2를 매핑
        entityManager.persist(GroupMapping.builder().phoneBook(testPhoneBook).recipient(recipient1).build());
        entityManager.persist(GroupMapping.builder().phoneBook(testPhoneBook).recipient(recipient2).build());

        // anotherPhoneBook에는 recipient3를 매핑
        entityManager.persist(GroupMapping.builder().phoneBook(anotherPhoneBook).recipient(recipient3).build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("특정 주소록에 매핑된 수신자 ID 목록 조회 테스트")
    void findRecipientIdsByPhoneBook_Test() {
        // given
        // 1. @BeforeEach에서 testPhoneBook에 recipient1, recipient2가 매핑된 상태입니다.

        // when
        // 1. 테스트 대상 메서드를 호출하여 특정 주소록에 속한 수신자 ID 목록을 조회합니다.
        List<Integer> recipientIds = groupMappingRepository.findRecipientIdsByPhoneBook(testPhoneBook);

        // then
        // 1. 조회된 ID 목록이 null이 아닌지 확인합니다.
        assertThat(recipientIds).isNotNull();
        // 2. 조회된 ID의 개수가 2개인지 확인합니다.
        assertThat(recipientIds.size()).isEqualTo(2);
        // 3. 조회된 ID 목록에 예상했던 수신자 ID들이 모두 포함되어 있는지 순서에 상관없이 확인합니다.
        assertThat(recipientIds).containsExactlyInAnyOrder(recipient1.getRecipientId(), recipient2.getRecipientId());
    }

    @Test
    @DisplayName("매핑된 수신자가 없는 주소록 조회 시 빈 목록 반환 테스트")
    void findRecipientIdsByPhoneBook_Empty_Test() {
        // given
        // 1. 테스트용 워크스페이스를 조회합니다.
        Workspace workspace = entityManager.find(Workspace.class, testPhoneBook.getWorkspace().getWorkspaceId());
        // 2. 수신자가 매핑되지 않은 새로운 주소록을 생성하고 저장합니다.
        PhoneBook emptyPhoneBook = PhoneBook.builder().workspace(workspace).phoneBookName("empty-phonebook").build();
        entityManager.persistAndFlush(emptyPhoneBook);

        // when
        // 1. 비어있는 주소록을 대상으로 메서드를 호출합니다.
        List<Integer> recipientIds = groupMappingRepository.findRecipientIdsByPhoneBook(emptyPhoneBook);

        // then
        // 1. 반환된 목록이 null이 아닌지 확인합니다.
        assertThat(recipientIds).isNotNull();
        // 2. 반환된 목록이 비어있는지 확인합니다.
        assertThat(recipientIds).isEmpty();
    }

    @Test
    @DisplayName("벌크 INSERT로 여러 수신자를 주소록에 일괄 추가 테스트")
    void bulkInsertMappings_Test() {
        // given
        // 1. 새로운 수신자들을 생성합니다.
        Workspace workspace = entityManager.find(Workspace.class, testPhoneBook.getWorkspace().getWorkspaceId());

        Recipient newRecipient1 = Recipient.builder()
                .recipientName("신규수신자1")
                .recipientPhoneNumber("010-2222-2222")
                .workspace(workspace)
                .build();
        entityManager.persist(newRecipient1);

        Recipient newRecipient2 = Recipient.builder()
                .recipientName("신규수신자2")
                .recipientPhoneNumber("010-3333-3333")
                .workspace(workspace)
                .build();
        entityManager.persist(newRecipient2);

        entityManager.flush();

        // 2. 벌크 INSERT에 사용할 수신자 ID 목록과 타임스탬프를 준비합니다.
        List<Integer> recipientIds = List.of(newRecipient1.getRecipientId(), newRecipient2.getRecipientId());
        LocalDateTime timestamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();

        // when
        // 1. 벌크 INSERT를 실행합니다.
        groupMappingRepository.bulkInsertMappings(testPhoneBook.getPhoneBookId(), recipientIds, timestamp);
        entityManager.flush();
        entityManager.clear();

        // then
        // 1. 주소록에 새로운 수신자들이 추가되었는지 확인합니다.
        List<Integer> allRecipientIds = groupMappingRepository.findRecipientIdsByPhoneBook(testPhoneBook);
        assertThat(allRecipientIds).hasSize(4); // 기존 2개 + 신규 2개
        assertThat(allRecipientIds).contains(newRecipient1.getRecipientId(), newRecipient2.getRecipientId());
    }

    @Test
    @DisplayName("벌크 INSERT 후 생성된 매핑들을 JPA 쿼리 메서드로 조회하는 테스트")
    void findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn_Test() {
        // given
        // 1. 새로운 수신자들을 생성합니다.
        Workspace workspace = entityManager.find(Workspace.class, testPhoneBook.getWorkspace().getWorkspaceId());

        Recipient newRecipient1 = Recipient.builder()
                .recipientName("신규수신자1")
                .recipientPhoneNumber("010-2222-2222")
                .workspace(workspace)
                .build();
        entityManager.persist(newRecipient1);

        Recipient newRecipient2 = Recipient.builder()
                .recipientName("신규수신자2")
                .recipientPhoneNumber("010-3333-3333")
                .workspace(workspace)
                .build();
        entityManager.persist(newRecipient2);

        entityManager.flush();

        // 2. 벌크 INSERT로 매핑을 생성합니다.
        List<Integer> recipientIds = List.of(newRecipient1.getRecipientId(), newRecipient2.getRecipientId());
        LocalDateTime timestamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        groupMappingRepository.bulkInsertMappings(testPhoneBook.getPhoneBookId(), recipientIds, timestamp);
        entityManager.flush();
        entityManager.clear();

        // when
        // 1. JPA 쿼리 메서드로 생성된 매핑들을 조회합니다.
        List<GroupMapping> savedMappings = groupMappingRepository.findAllByPhoneBook_PhoneBookIdAndRecipient_RecipientIdIn(
                testPhoneBook.getPhoneBookId(), recipientIds);

        // then
        // 1. 조회된 매핑의 개수가 올바른지 확인합니다.
        assertThat(savedMappings).hasSize(2);
        // 2. 조회된 매핑들이 올바른 수신자 ID를 가지고 있는지 확인합니다.
        assertThat(savedMappings).extracting("recipient.recipientId")
                .containsExactlyInAnyOrder(newRecipient1.getRecipientId(), newRecipient2.getRecipientId());
        // 3. 모든 매핑이 삭제되지 않은 상태인지 확인합니다 (@SQLRestriction 자동 적용).
        assertThat(savedMappings).allMatch(mapping -> !mapping.getIsDeleted());
        // 4. 모든 매핑의 생성 시간이 설정되어 있는지 확인합니다.
        assertThat(savedMappings).allMatch(mapping -> mapping.getCreatedAt() != null);
        // 5. 조회된 매핑들이 올바른 주소록에 속해 있는지 확인합니다.
        assertThat(savedMappings).allMatch(mapping -> mapping.getPhoneBook().getPhoneBookId().equals(testPhoneBook.getPhoneBookId()));
    }

    @Test
    @DisplayName("특정 주소록의 GroupMapping 페이징 조회 테스트 - 수신자 생성 시간 내림차순 정렬")
    void findByPhoneBookOrderByRecipient_CreatedAtDescRecipient_RecipientIdDesc_Pageable_Test() {
        // given
        // 1. @BeforeEach에서 testPhoneBook에 recipient1, recipient2가 매핑된 상태입니다.
        // 2. 페이징 정보를 설정합니다. (페이지 크기 10, 첫 번째 페이지)
        Pageable pageable = PageRequest.of(0, 10);

        // when
        // 1. 테스트 대상 메서드를 호출하여 특정 주소록의 GroupMapping을 페이징 조회합니다.
        Page<GroupMapping> groupMappingPage = groupMappingRepository.findByPhoneBookOrderByRecipient_CreatedAtDescRecipient_RecipientIdDesc(testPhoneBook, pageable);

        // then
        // 1. 페이지 객체가 null이 아닌지 확인합니다.
        assertThat(groupMappingPage).isNotNull();
        // 2. 전체 요소 개수가 2개인지 확인합니다.
        assertThat(groupMappingPage.getTotalElements()).isEqualTo(2);
        // 3. 현재 페이지의 요소 개수가 2개인지 확인합니다.
        assertThat(groupMappingPage.getNumberOfElements()).isEqualTo(2);
        // 4. 조회된 GroupMapping들이 올바른 주소록에 속하는지 확인합니다.
        assertThat(groupMappingPage.getContent())
                .allMatch(gm -> gm.getPhoneBook().getPhoneBookId().equals(testPhoneBook.getPhoneBookId()));
        // 5. 조회된 GroupMapping들의 수신자가 recipient1, recipient2인지 확인합니다.
        assertThat(groupMappingPage.getContent())
                .extracting(gm -> gm.getRecipient().getRecipientId())
                .containsExactlyInAnyOrder(recipient1.getRecipientId(), recipient2.getRecipientId());
    }

    @Test
    @DisplayName("특정 주소록의 GroupMapping 페이징 조회 테스트 - 수신자 생성 시간 정렬 확인")
    void findByPhoneBookOrderByRecipient_CreatedAtDescRecipient_RecipientIdDesc_SortByRecipientCreatedAt_Test() {
        // given
        // 1. Detached 상태인 testPhoneBook을 DB에서 다시 조회하여 Managed 상태로 만듭니다.
        PhoneBook managedTestPhoneBook = entityManager.find(PhoneBook.class, testPhoneBook.getPhoneBookId());
        // 2. recipient3을 Managed 상태의 주소록에 추가합니다.
        entityManager.persist(GroupMapping.builder().phoneBook(managedTestPhoneBook).recipient(recipient3).build());
        entityManager.flush();

        // 3. 페이징 정보를 설정합니다.
        Pageable pageable = PageRequest.of(0, 10);

        // when
        // 1. 테스트 대상 메서드를 호출합니다.
        Page<GroupMapping> groupMappingPage = groupMappingRepository.findByPhoneBookOrderByRecipient_CreatedAtDescRecipient_RecipientIdDesc(managedTestPhoneBook, pageable);

        // then
        // 1. 전체 요소 개수가 3개인지 확인합니다.
        assertThat(groupMappingPage.getTotalElements()).isEqualTo(3);
        // 2. 첫 번째 요소가 가장 최신에 생성된 recipient3인지 확인합니다.
        assertThat(groupMappingPage.getContent().getFirst().getRecipient().getRecipientId())
                .isEqualTo(recipient3.getRecipientId());
    }

    @Test
    @DisplayName("GroupMapping이 없는 주소록 페이징 조회 시 빈 페이지 반환 테스트")
    void findByPhoneBookOrderByRecipient_CreatedAtDescRecipient_RecipientIdDesc_EmptyPage_Test() {
        // given
        // 1. 테스트용 워크스페이스를 조회합니다.
        Workspace workspace = entityManager.find(Workspace.class, testPhoneBook.getWorkspace().getWorkspaceId());
        // 2. GroupMapping이 없는 새로운 주소록을 생성하고 저장합니다.
        PhoneBook emptyPhoneBook = PhoneBook.builder().workspace(workspace).phoneBookName("empty-phonebook").build();
        entityManager.persistAndFlush(emptyPhoneBook);
        // 3. 페이징 정보를 설정합니다.
        Pageable pageable = PageRequest.of(0, 10);

        // when
        // 1. 비어있는 주소록을 대상으로 메서드를 호출합니다.
        Page<GroupMapping> groupMappingPage = groupMappingRepository.findByPhoneBookOrderByRecipient_CreatedAtDescRecipient_RecipientIdDesc(emptyPhoneBook, pageable);

        // then
        // 1. 페이지 객체가 null이 아닌지 확인합니다.
        assertThat(groupMappingPage).isNotNull();
        // 2. 전체 요소 개수가 0개인지 확인합니다.
        assertThat(groupMappingPage.getTotalElements()).isEqualTo(0);
        // 3. 현재 페이지의 요소 개수가 0개인지 확인합니다.
        assertThat(groupMappingPage.getNumberOfElements()).isEqualTo(0);
        // 4. 콘텐츠가 비어있는지 확인합니다.
        assertThat(groupMappingPage.getContent()).isEmpty();
    }
}
