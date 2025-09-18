package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GroupMappingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GroupMappingRepository groupMappingRepository;

    private PhoneBook testPhoneBook;
    private Recipient recipient1, recipient2;

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

        PhoneBook anotherPhoneBook = PhoneBook.builder()
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

        Recipient recipient3 = Recipient.builder()
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
}
