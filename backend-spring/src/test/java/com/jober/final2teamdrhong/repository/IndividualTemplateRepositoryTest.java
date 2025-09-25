package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.Workspace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class IndividualTemplateRepositoryTest {

    @Autowired
    private IndividualTemplateRepository individualTemplateRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByIdOrThrow - 존재하는 ID 조회 성공")
    void findByIdOrThrow_success() {
        // given
        User user = userRepository.save(User.builder()
                .userName("테스터")
                .userEmail("tester@example.com")
                .userNumber("010-1111-2222")
                .build());

        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("http://test.com")
                .representerName("홍길동")
                .representerPhoneNumber("010-9999-8888")
                .companyName("테스트회사")
                .user(user)
                .build());

        IndividualTemplate template = individualTemplateRepository.save(
                IndividualTemplate.builder()
                        .workspace(workspace)
                        .individualTemplateTitle("제목1")
                        .individualTemplateContent("내용1")
                        .buttonTitle("버튼1")
                        .status(IndividualTemplate.Status.DRAFT)
                        .build()
        );

        // when
        IndividualTemplate found = individualTemplateRepository.findByIdOrThrow(template.getIndividualTemplateId());

        // then
        assertThat(found.getIndividualTemplateTitle()).isEqualTo("제목1");
        assertThat(found.getWorkspace().getWorkspaceId()).isEqualTo(workspace.getWorkspaceId());
    }

    @Test
    @DisplayName("findByWorkspace_WorkspaceId - 워크스페이스별 조회 성공")
    void findByWorkspaceId_success() {
        // given
        User user = userRepository.save(User.builder()
                .userName("테스터")
                .userEmail("tester@example.com")
                .userNumber("010-1111-2222")
                .build());

        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("http://test.com")
                .representerName("홍길동")
                .representerPhoneNumber("010-9999-8888")
                .companyName("테스트회사")
                .user(user)
                .build());

        individualTemplateRepository.save(IndividualTemplate.builder()
                .workspace(workspace)
                .individualTemplateTitle("템플릿A")
                .status(IndividualTemplate.Status.DRAFT)
                .build());

        // when
        Page<IndividualTemplate> page = individualTemplateRepository.findByWorkspace_WorkspaceId(
                workspace.getWorkspaceId(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(page).hasSize(1);
        assertThat(page.getContent().get(0).getIndividualTemplateTitle()).isEqualTo("템플릿A");
    }

    @Test
    @DisplayName("findByWorkspace_WorkspaceIdAndStatus - 워크스페이스 + 상태별 조회 성공")
    void findByWorkspaceIdAndStatus_success() {
        // given
        User user = userRepository.save(User.builder()
                .userName("테스터")
                .userEmail("tester@example.com")
                .userNumber("010-1111-2222")
                .build());

        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("http://test.com")
                .representerName("홍길동")
                .representerPhoneNumber("010-9999-8888")
                .companyName("테스트회사")
                .user(user)
                .build());

        individualTemplateRepository.save(IndividualTemplate.builder()
                .workspace(workspace)
                .individualTemplateTitle("템플릿B")
                .status(IndividualTemplate.Status.APPROVED)
                .build());

        // when
        Page<IndividualTemplate> page = individualTemplateRepository.findByWorkspace_WorkspaceIdAndStatus(
                workspace.getWorkspaceId(),
                IndividualTemplate.Status.APPROVED,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(page).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(IndividualTemplate.Status.APPROVED);
    }

    @Test
    @DisplayName("findByIndividualTemplateIdAndWorkspace_WorkspaceId - 템플릿 단일 조회 성공")
    void findByIdAndWorkspaceId_success() {
        // given
        User user = userRepository.save(User.builder()
                .userName("테스터")
                .userEmail("tester@example.com")
                .userNumber("010-1111-2222")
                .build());

        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("http://test.com")
                .representerName("홍길동")
                .representerPhoneNumber("010-9999-8888")
                .companyName("테스트회사")
                .user(user)
                .build());

        IndividualTemplate saved = individualTemplateRepository.save(IndividualTemplate.builder()
                .workspace(workspace)
                .individualTemplateTitle("템플릿C")
                .status(IndividualTemplate.Status.PENDING)
                .build());

        // when
        Optional<IndividualTemplate> found = individualTemplateRepository
                .findByIndividualTemplateIdAndWorkspace_WorkspaceId(saved.getIndividualTemplateId(), workspace.getWorkspaceId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getIndividualTemplateTitle()).isEqualTo("템플릿C");
    }
}
