package com.jober.final2teamdrhong.repository;

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

@DataJpaTest // JPA 관련 컴포넌트만 로드하여 테스트, 인메모리 DB 사용
class WorkspaceRepositoryTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private TestEntityManager entityManager; // 테스트용 엔티티 관리자, 데이터 준비에 유용

    private User testUser;
    private User anotherUser;

    @BeforeEach // 각 테스트 실행 전에 테스트용 사용자 데이터를 미리 생성
    void setUp() {
        testUser = User.builder()
                .userName("테스트유저")
                .userEmail("test@example.com")
                .userNumber("010-1234-5678")
                .build();
        entityManager.persist(testUser); // TestEntityManager를 사용해 User를 DB에 저장
        
        anotherUser = User.builder()
                .userName("테스트유저2")
                .userEmail("test2@example.com")
                .userNumber("010-1111-2222")
                .build();
        entityManager.persist(anotherUser);
    }

    @Test
    @DisplayName("URL 존재 여부 확인 테스트")
    void existsByWorkspaceUrl_Test() {
        // given
        // 1. user 필드를 제외하고 빌더로 Workspace 객체를 생성합니다.
        //    (빌더에 포함된 @NonNull 필드는 필수로 넣어주어야 합니다.)
        Workspace newWorkspace = Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("테스트대표")
                .representerPhoneNumber("010-0000-0000")
                .companyName("테스트회사")
                .user(testUser)
                .build();

        // 2. 생성한 엔티티를 DB에 저장합니다.
        workspaceRepository.save(newWorkspace);

        // when
        // 1. 테스트할 메소드를 호출합니다.
        //    - DB에 방금 저장한 URL로 호출해봅니다.
        boolean shouldBeTrue = workspaceRepository.existsByWorkspaceUrl("test-url");
        //    - DB에 절대 없을 법한 임의의 URL로 호출해봅니다.
        boolean shouldBeFalse = workspaceRepository.existsByWorkspaceUrl("non-existing-url");

        // then
        // 1. 결과를 검증합니다.
        //    - existingUrl로 조회한 결과는 반드시 true여야 합니다.
        assertThat(shouldBeTrue).isTrue();
        //    - non-existing-url로 조회한 결과는 반드시 false여야 합니다.
        assertThat(shouldBeFalse).isFalse();
    }

    @Test
    @DisplayName("특정 사용자가 소유한 모든 워크스페이스 목록 조회 테스트")
    void findAllByUser_UserId_Test() {
        // given
        // 1. testUser 소유의 워크스페이스 2개 생성
        Workspace testWorkspace1 = Workspace.builder()
                .workspaceName("테스트 워크스페이스1")
                .workspaceUrl("test-url-1")
                .representerName("테스트대표1")
                .representerPhoneNumber("010-1111-1111")
                .companyName("테스트회사1")
                .build();
        Workspace testWorkspace2 = Workspace.builder()
                .workspaceName("테스트 워크스페이스2")
                .workspaceUrl("test-url-2")
                .representerName("테스트대표1")
                .representerPhoneNumber("010-1111-1111")
                .companyName("테스트회사2")
                .build();
        // anotherUser 소유의 워크스페이스 1개 생성
        Workspace testWorkspace3 = Workspace.builder()
                .workspaceName("테스트 워크스페이스3")
                .workspaceUrl("test-url-3")
                .representerName("테스트대표2")
                .representerPhoneNumber("010-2222-2222")
                .companyName("테스트회사3")
                .build();

        // 2. persist 하기 전에 setUser로 연관관계 설정
        testWorkspace1.setUser(testUser);
        testWorkspace2.setUser(testUser);
        testWorkspace3.setUser(anotherUser);

        // 3. 연관관계 설정이 완료된 객체를 persist
        // TestEntityManager를 사용해 Workspace를 DB에 저장
        entityManager.persist(testWorkspace1);
        entityManager.persist(testWorkspace2);
        entityManager.persist(testWorkspace3);

        // 4. when 단계에서 SELECT 하기 전, DB에 변경사항을 강제 동기화
        entityManager.flush();

        // when
        // 테스트 유저의 ID로 워크스페이스 목록 조회
        List<Workspace> result = workspaceRepository.findAllByUser_UserId(testUser.getUserId());

        // then
        assertThat(result).hasSize(2); // 결과 목록의 크기는 2여야 함
        assertThat(result).extracting(Workspace::getWorkspaceName) // 이름만 추출
                .containsExactlyInAnyOrder("테스트 워크스페이스1", "테스트 워크스페이스2"); // 이름이 일치하는지 확인
    }

    @Test
    @DisplayName("특정 사용자가 소유한 특정 워크스페이스 상세 조회 테스트")
    void findByWorkspaceIdAndUser_UserId_Test() {
        // given
        // 1. testUser 소유의 워크스페이스 생성
        Workspace testWorkspace1 = Workspace.builder()
                .workspaceName("테스트 워크스페이스1")
                .workspaceUrl("test-url-1")
                .representerName("테스트대표1")
                .representerPhoneNumber("010-1111-1111")
                .companyName("테스트회사1")
                .build();
        testWorkspace1.setUser(testUser);
        entityManager.persist(testWorkspace1);

        // 2. anotherUser 소유의 워크스페이스 1개 생성
        Workspace testWorkspace2 = Workspace.builder()
                .workspaceName("테스트 워크스페이스2")
                .workspaceUrl("test-url-2")
                .representerName("테스트대표2")
                .representerPhoneNumber("010-2222-2222")
                .companyName("테스트회사2")
                .build();
        testWorkspace2.setUser(anotherUser);
        entityManager.persist(testWorkspace2);

        // 3. when 단계에서 SELECT 하기 전, DB에 변경사항을 강제 동기화
        entityManager.flush();

        // when
        // 1. 성공 케이스: 내 워크스페이스 ID와 내 유저 ID로 조회
        Optional<Workspace> successCase =
                workspaceRepository.findByWorkspaceIdAndUser_UserId(testWorkspace1.getWorkspaceId(), testUser.getUserId());

        // 2. 실패 케이스: 남의 워크스페이스 ID와 내 유저 ID로 조회
        Optional<Workspace> failCase =
                workspaceRepository.findByWorkspaceIdAndUser_UserId(testWorkspace2.getWorkspaceId(), testUser.getUserId());

        // then
        // 1. 성공 케이스 검증
        assertThat(successCase).isPresent(); // Optional이 비어있지 않은지 확인
        assertThat(successCase.get().getWorkspaceName()).isEqualTo("테스트 워크스페이스1"); // 내용이 올바른지 확인

        // 2. 실패 케이스 검증
        assertThat(failCase).isNotPresent(); // Optional이 비어있는지 확인 (남의 것은 조회되면 안 됨)
    }
}