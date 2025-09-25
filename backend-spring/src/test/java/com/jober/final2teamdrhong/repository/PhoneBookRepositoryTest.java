package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.PhoneBook;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PhoneBookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PhoneBookRepository phoneBookRepository;

    private Workspace testWorkspace;
    private Workspace anotherWorkspace;
    private PhoneBook testPhoneBook;
    private PhoneBook testPhoneBook2;
    private PhoneBook anotherWorkspacePhoneBook;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .userName("user")
                .userEmail("user@test.com")
                .build();
        entityManager.persist(user);

        testWorkspace = Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("테스트대표")
                .representerPhoneNumber("010-0000-0000")
                .companyName("테스트회사")
                .user(user)
                .build();
        entityManager.persist(testWorkspace);

        anotherWorkspace = Workspace.builder()
                .workspaceName("테스트 워크스페이스2")
                .workspaceUrl("test-url-2")
                .representerName("테스트대표")
                .representerPhoneNumber("010-0000-0000")
                .companyName("테스트회사")
                .user(user)
                .build();
        entityManager.persist(anotherWorkspace);

        testPhoneBook = PhoneBook.builder()
                .phoneBookName("test-phonebook")
                .workspace(testWorkspace)
                .build();
        entityManager.persist(testPhoneBook);

        testPhoneBook2 = PhoneBook.builder()
                .phoneBookName("test-phonebook-2")
                .workspace(testWorkspace)
                .build();
        entityManager.persist(testPhoneBook2);

        anotherWorkspacePhoneBook = PhoneBook.builder()
                .phoneBookName("another-workspace-phonebook")
                .workspace(anotherWorkspace)
                .build();
        entityManager.persist(anotherWorkspacePhoneBook);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("주소록 ID와 워크스페이스 ID로 주소록 조회 성공 테스트")
    void findByPhoneBookIdAndWorkspace_WorkspaceId_Success_Test() {
        // given
        // 1. @BeforeEach에서 testPhoneBook이 testWorkspace에 속하도록 설정되어 있습니다.

        // when
        // 1. 테스트 대상 메서드를 올바른 주소록 ID와 워크스페이스 ID로 호출합니다.
        Optional<PhoneBook> foundPhoneBookOpt = phoneBookRepository.findByPhoneBookIdAndWorkspace_WorkspaceId(
                testPhoneBook.getPhoneBookId(), testWorkspace.getWorkspaceId());

        // then
        // 1. Optional 객체가 비어있지 않은지(조회 성공) 확인합니다.
        assertThat(foundPhoneBookOpt).isPresent();
        // 2. 조회된 주소록의 ID가 예상과 일치하는지 확인합니다.
        assertThat(foundPhoneBookOpt.get().getPhoneBookId()).isEqualTo(testPhoneBook.getPhoneBookId());
        // 3. 조회된 주소록이 속한 워크스페이스의 ID가 예상과 일치하는지 확인합니다.
        assertThat(foundPhoneBookOpt.get().getWorkspace().getWorkspaceId()).isEqualTo(testWorkspace.getWorkspaceId());
    }

    @Test
    @DisplayName("주소록 ID와 워크스페이스 ID로 주소록 조회 실패 테스트 - 워크스페이스 불일치")
    void findByPhoneBookIdAndWorkspace_WorkspaceId_Fail_WorkspaceMismatch_Test() {
        // given
        // 1. @BeforeEach에서 testPhoneBook은 testWorkspace에 속해 있습니다.

        // when
        // 1. 테스트 대상 메서드를 호출하되, 다른 워크스페이스 ID(anotherWorkspace)를 사용합니다.
        Optional<PhoneBook> foundPhoneBookOpt = phoneBookRepository.findByPhoneBookIdAndWorkspace_WorkspaceId(
                testPhoneBook.getPhoneBookId(), anotherWorkspace.getWorkspaceId());

        // then
        // 1. Optional 객체가 비어있는지(조회 실패) 확인합니다.
        assertThat(foundPhoneBookOpt).isNotPresent();
    }

    @Test
    @DisplayName("주소록 ID와 워크스페이스 ID로 주소록 조회 실패 테스트 - 존재하지 않는 주소록 ID")
    void findByPhoneBookIdAndWorkspace_WorkspaceId_Fail_PhoneBookNotFound_Test() {
        // given
        // 1. DB에 존재하지 않을 임의의 ID를 설정합니다.
        Integer nonExistentPhoneBookId = -1;

        // when
        // 1. 존재하지 않는 주소록 ID로 메서드를 호출합니다.
        Optional<PhoneBook> foundPhoneBookOpt = phoneBookRepository.findByPhoneBookIdAndWorkspace_WorkspaceId(
                nonExistentPhoneBookId, testWorkspace.getWorkspaceId());

        // then
        // 1. Optional 객체가 비어있는지(조회 실패) 확인합니다.
        assertThat(foundPhoneBookOpt).isNotPresent();
    }

    @Test
    @DisplayName("워크스페이스별 주소록 목록 조회 성공 테스트")
    void findAllByWorkspace_WorkspaceId_Success_Test() {
        // given
        // 1. @BeforeEach에서 testWorkspace에 testPhoneBook, testPhoneBook2가 속하도록 설정되어 있습니다.

        // when
        // 1. 테스트 대상 메서드를 testWorkspace ID로 호출합니다.
        List<PhoneBook> phoneBookList = phoneBookRepository.findAllByWorkspace_WorkspaceId(testWorkspace.getWorkspaceId());

        // then
        // 1. 조회된 주소록 목록의 크기가 2개인지 확인합니다.
        assertThat(phoneBookList).hasSize(2);
        // 2. 조회된 주소록들이 testPhoneBook, testPhoneBook2를 포함하는지 확인합니다.
        assertThat(phoneBookList)
                .extracting(PhoneBook::getPhoneBookId)
                .containsExactlyInAnyOrder(testPhoneBook.getPhoneBookId(), testPhoneBook2.getPhoneBookId());
        // 3. 모든 주소록이 testWorkspace에 속하는지 확인합니다.
        assertThat(phoneBookList)
                .allMatch(phoneBook -> phoneBook.getWorkspace().getWorkspaceId().equals(testWorkspace.getWorkspaceId()));
    }

    @Test
    @DisplayName("워크스페이스별 주소록 목록 조회 테스트 - 다른 워크스페이스 주소록 제외")
    void findAllByWorkspace_WorkspaceId_ExcludeOtherWorkspace_Test() {
        // given
        // 1. @BeforeEach에서 anotherWorkspace에 anotherWorkspacePhoneBook이 속하도록 설정되어 있습니다.

        // when
        // 1. 테스트 대상 메서드를 anotherWorkspace ID로 호출합니다.
        List<PhoneBook> phoneBookList = phoneBookRepository.findAllByWorkspace_WorkspaceId(anotherWorkspace.getWorkspaceId());

        // then
        // 1. 조회된 주소록 목록의 크기가 1개인지 확인합니다.
        assertThat(phoneBookList).hasSize(1);
        // 2. 조회된 주소록이 anotherWorkspacePhoneBook인지 확인합니다.
        assertThat(phoneBookList.getFirst().getPhoneBookId()).isEqualTo(anotherWorkspacePhoneBook.getPhoneBookId());
        // 3. testWorkspace의 주소록들이 포함되지 않았는지 확인합니다.
        assertThat(phoneBookList)
                .extracting(PhoneBook::getPhoneBookId)
                .doesNotContain(testPhoneBook.getPhoneBookId(), testPhoneBook2.getPhoneBookId());
    }

    @Test
    @DisplayName("워크스페이스별 주소록 목록 조회 테스트 - 존재하지 않는 워크스페이스")
    void findAllByWorkspace_WorkspaceId_NotFound_Test() {
        // given
        // 1. DB에 존재하지 않을 임의의 워크스페이스 ID를 설정합니다.
        Integer nonExistentWorkspaceId = -1;

        // when
        // 1. 존재하지 않는 워크스페이스 ID로 메서드를 호출합니다.
        List<PhoneBook> phoneBookList = phoneBookRepository.findAllByWorkspace_WorkspaceId(nonExistentWorkspaceId);

        // then
        // 1. 조회된 주소록 목록이 비어있는지 확인합니다.
        assertThat(phoneBookList).isEmpty();
    }

    @Test
    @DisplayName("소프트 딜리트된 주소록 포함 조회 성공 테스트 - 정상 상태 주소록")
    void findByIdIncludingDeleted_Success_NormalPhoneBook_Test() {
        // given
        // 1. @BeforeEach에서 생성된 일반적인 상태의 testPhoneBook을 사용합니다.

        // when
        // 1. findByIdIncludingDeleted 메서드로 정상 상태의 주소록을 조회합니다.
        Optional<PhoneBook> foundPhoneBookOpt = phoneBookRepository.findByIdIncludingDeleted(testPhoneBook.getPhoneBookId());

        // then
        // 1. Optional 객체가 비어있지 않은지(조회 성공) 확인합니다.
        assertThat(foundPhoneBookOpt).isPresent();
        // 2. 조회된 주소록의 ID가 예상과 일치하는지 확인합니다.
        assertThat(foundPhoneBookOpt.get().getPhoneBookId()).isEqualTo(testPhoneBook.getPhoneBookId());
        // 3. 조회된 주소록이 소프트 딜리트되지 않았는지 확인합니다.
        assertThat(foundPhoneBookOpt.get().getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("소프트 딜리트된 주소록 포함 조회 성공 테스트 - 소프트 딜리트된 주소록")
    void findByIdIncludingDeleted_Success_SoftDeletedPhoneBook_Test() {
        // given
        // 1. setUp에 있는 testPhoneBook을 소프트 딜리트 처리합니다.
        PhoneBook phoneBookToDelete = entityManager.find(PhoneBook.class, testPhoneBook.getPhoneBookId());
        phoneBookToDelete.softDelete();
        entityManager.persist(phoneBookToDelete);
        entityManager.flush();
        entityManager.clear();

        // 2. 일반적인 findById로는 조회되지 않는지 확인합니다.
        Optional<PhoneBook> normalFindResult = phoneBookRepository.findById(testPhoneBook.getPhoneBookId());
        assertThat(normalFindResult).isNotPresent(); // @SQLRestriction으로 인해 조회되지 않음

        // when
        // 1. findByIdIncludingDeleted 메서드로 소프트 딜리트된 주소록을 조회합니다.
        Optional<PhoneBook> foundPhoneBookOpt = phoneBookRepository.findByIdIncludingDeleted(testPhoneBook.getPhoneBookId());

        // then
        // 1. Optional 객체가 비어있지 않은지(조회 성공) 확인합니다.
        assertThat(foundPhoneBookOpt).isPresent();
        // 2. 조회된 주소록의 ID가 예상과 일치하는지 확인합니다.
        assertThat(foundPhoneBookOpt.get().getPhoneBookId()).isEqualTo(testPhoneBook.getPhoneBookId());
        // 3. 조회된 주소록이 소프트 딜리트 상태인지 확인합니다.
        assertThat(foundPhoneBookOpt.get().getDeletedAt()).isNotNull();
        // 4. isDeleted 필드가 true인지 확인합니다.
        assertThat(foundPhoneBookOpt.get().getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("소프트 딜리트된 주소록 포함 조회 실패 테스트 - 존재하지 않는 주소록 ID")
    void findByIdIncludingDeleted_Fail_PhoneBookNotFound_Test() {
        // given
        // 1. DB에 존재하지 않을 임의의 ID를 설정합니다.
        Integer nonExistentPhoneBookId = -1;

        // when
        // 1. 존재하지 않는 주소록 ID로 메서드를 호출합니다.
        Optional<PhoneBook> foundPhoneBookOpt = phoneBookRepository.findByIdIncludingDeleted(nonExistentPhoneBookId);

        // then
        // 1. Optional 객체가 비어있는지(조회 실패) 확인합니다.
        assertThat(foundPhoneBookOpt).isNotPresent();
    }
}
