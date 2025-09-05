package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // JPA 관련 컴포넌트만 로드하여 테스트, 인메모리 DB 사용
class WorkspaceRepositoryTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private TestEntityManager entityManager; // 테스트용 엔티티 관리자, 데이터 준비에 유용

    private User testUser;

    @BeforeEach // 각 테스트 실행 전에 테스트용 사용자 데이터를 미리 생성
    void setUp() {
        testUser = User.builder()
                .userName("테스트유저")
                .userEmail("test@example.com")
                .userNumber("010-1234-5678")
                .build();
        entityManager.persist(testUser); // TestEntityManager를 사용해 User를 DB에 저장
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
                .build();

        // 2. 생성된 객체에 setUser() 메소드를 사용해 User를 연결합니다.
        newWorkspace.setUser(testUser);

        // 3. 생성한 엔티티를 DB에 저장합니다.
        workspaceRepository.save(newWorkspace);

        // when
        // 4. 테스트할 메소드를 호출합니다.
        //    - DB에 방금 저장한 URL로 호출해봅니다.
        boolean shouldBeTrue = workspaceRepository.existsByWorkspaceUrl("test-url");
        //    - DB에 절대 없을 법한 임의의 URL로 호출해봅니다.
        boolean shouldBeFalse = workspaceRepository.existsByWorkspaceUrl("non-existing-url");

        // then
        // 5. 결과를 검증합니다.
        //    - existingUrl로 조회한 결과는 반드시 true여야 합니다.
        assertThat(shouldBeTrue).isTrue();
        //    - non-existing-url로 조회한 결과는 반드시 false여야 합니다.
        assertThat(shouldBeFalse).isFalse();
    }
}