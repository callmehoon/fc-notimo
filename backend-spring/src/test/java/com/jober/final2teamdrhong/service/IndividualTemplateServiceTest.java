package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class IndividualTemplateServiceTest {

    private static final Logger log = LoggerFactory.getLogger(IndividualTemplateServiceTest.class);

    @Mock
    private IndividualTemplateRepository individualTemplateRepo;

    @Mock
    private WorkspaceRepository workspaceRepo;

    @InjectMocks
    private IndividualTemplateService service;

    private Workspace workspaceMock;

    @BeforeEach
    void setUp() {
        // Workspace 엔티티의 실제 생성 방식을 모르는 상황이므로, 안전하게 mock 처리
        workspaceMock = mock(Workspace.class);
    }

    @Nested
    @DisplayName("validateWorkspaceOwnership")
    class ValidateWorkspaceOwnership {

        @Test
        @DisplayName("userId가 null이면 AccessDeniedException이 발생한다")
        void validateWorkspaceOwnership_nullUserId() {
            // when & then
            assertThatThrownBy(() -> service.validateWorkspaceOwnership(1, null))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("인증이 필요합니다.");

            verifyNoInteractions(workspaceRepo);
        }

        @Test
        @DisplayName("워크스페이스 소유권이 없으면 AccessDeniedException이 발생한다")
        void validateWorkspaceOwnership_noOwnership() {
            // given
            when(workspaceRepo.existsByWorkspaceIdAndUser_UserId(1, 100))
                    .thenReturn(false);

            // when & then
            assertThatThrownBy(() -> service.validateWorkspaceOwnership(1, 100))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("해당 워크스페이스에 접근 권한이 없습니다.");

            verify(workspaceRepo, times(1)).existsByWorkspaceIdAndUser_UserId(1, 100);
            verifyNoMoreInteractions(workspaceRepo);
        }

        @Test
        @DisplayName("워크스페이스 소유권이 있으면 정상적으로 통과한다")
        void validateWorkspaceOwnership_hasOwnership() {
            // given
            when(workspaceRepo.existsByWorkspaceIdAndUser_UserId(1, 100))
                    .thenReturn(true);

            // when & then
            assertThatCode(() -> service.validateWorkspaceOwnership(1, 100))
                    .doesNotThrowAnyException();

            verify(workspaceRepo, times(1)).existsByWorkspaceIdAndUser_UserId(1, 100);
            verifyNoMoreInteractions(workspaceRepo);
        }
    }

    @Nested
    @DisplayName("createTemplate")
    class CreateTemplate {

        @Test
        @DisplayName("유효한 workspaceId면 빈(=null) 필드로 템플릿을 생성하고 응답을 반환한다")
        void createTemplate_success() {
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(1);
            when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspaceMock));

            // save()가 반환할 "저장된 엔티티" 모킹
            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate savedMock = mock(IndividualTemplate.class);
            when(savedMock.getIndividualTemplateId()).thenReturn(1);
            when(savedMock.getIndividualTemplateTitle()).thenReturn(null);
            when(savedMock.getIndividualTemplateContent()).thenReturn(null);
            when(savedMock.getButtonTitle()).thenReturn(null);
            when(savedMock.getWorkspace()).thenReturn(workspaceMock);
            when(savedMock.getCreatedAt()).thenReturn(now);
            when(savedMock.getUpdatedAt()).thenReturn(now);
            when(savedMock.getIsDeleted()).thenReturn(false);

            ArgumentCaptor<IndividualTemplate> captor = ArgumentCaptor.forClass(IndividualTemplate.class);
            when(individualTemplateRepo.save(captor.capture())).thenReturn(savedMock);

            // when
            IndividualTemplateResponse res = service.createTemplate(1);

            log.info("[동기] 생성된 IndividualTemplateResponse 정보:");
            log.info("  -> IndividualTemplate ID: {}", res.getIndividualTemplateId());
            log.info("  -> Workspace ID: {}", res.getWorkspaceId());
            log.info("  -> 템플릿 제목: {}", res.getIndividualTemplateTitle());
            log.info("  -> 템플릿 내용: {}", res.getIndividualTemplateContent());
            log.info("  -> 버튼 제목: {}", res.getButtonTitle());
            log.info("  -> 생성일: {}", res.getCreatedAt());
            log.info("  -> 수정일: {}", res.getUpdatedAt());

            // then
            IndividualTemplate toSave = captor.getValue();
            assertThat(toSave).isNotNull();
            assertThat(toSave.getWorkspace()).isSameAs(workspaceMock);
            assertThat(toSave.getIndividualTemplateTitle()).isNull();
            assertThat(toSave.getIndividualTemplateContent()).isNull();
            assertThat(toSave.getButtonTitle()).isNull();

            assertThat(res).isNotNull();
            assertThat(res.getIndividualTemplateId()).isEqualTo(1);
            assertThat(res.getIndividualTemplateTitle()).isNull();
            assertThat(res.getIndividualTemplateContent()).isNull();
            assertThat(res.getButtonTitle()).isNull();
            assertThat(res.getWorkspaceId()).isEqualTo(1);
            assertThat(res.getCreatedAt()).isEqualTo(now);
            assertThat(res.getUpdatedAt()).isEqualTo(now);
            assertThat(res.isDeleted()).isFalse();

            verify(workspaceRepo, times(1)).findById(1);
            verify(individualTemplateRepo, times(1)).save(any(IndividualTemplate.class));
            verifyNoMoreInteractions(workspaceRepo, individualTemplateRepo);
        }

        @Test
        @DisplayName("존재하지 않는 workspaceId면 IllegalArgumentException이 발생한다")
        void createTemplate_invalidWorkspace() {
            // given
            when(workspaceRepo.findById(999)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.createTemplate(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 workspaceId");

            verify(workspaceRepo, times(1)).findById(999);
            verifyNoInteractions(individualTemplateRepo);
        }
    }

    @Nested
    @DisplayName("createTemplateAsync")
    class CreateTemplateAsync {

        @Test
        @DisplayName("@Async 메서드는 CompletableFuture로 동일한 결과를 반환한다")
        void createTemplateAsync_success() throws Exception {
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(1);
            when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspaceMock));

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate savedMock = mock(IndividualTemplate.class);
            when(savedMock.getIndividualTemplateId()).thenReturn(456);
            when(savedMock.getIndividualTemplateTitle()).thenReturn(null);
            when(savedMock.getIndividualTemplateContent()).thenReturn(null);
            when(savedMock.getButtonTitle()).thenReturn(null);
            when(savedMock.getWorkspace()).thenReturn(workspaceMock);
            when(savedMock.getCreatedAt()).thenReturn(now);
            when(savedMock.getUpdatedAt()).thenReturn(now);
            when(savedMock.getIsDeleted()).thenReturn(false);

            when(individualTemplateRepo.save(any(IndividualTemplate.class))).thenReturn(savedMock);

            // when
            CompletableFuture<IndividualTemplateResponse> future = service.createTemplateAsync(1);

            // then - 여러 번 future.get() 호출 시 성능 이슈를 피하기 위해 한 번만 호출
            IndividualTemplateResponse res = future.get(2, TimeUnit.SECONDS);

            log.info("[비동기] 생성된 IndividualTemplateResponse 정보:");
            log.info("  -> IndividualTemplate ID: {}", res.getIndividualTemplateId());
            log.info("  -> Workspace ID: {}", res.getWorkspaceId());
            log.info("  -> 템플릿 제목: {}", res.getIndividualTemplateTitle());
            log.info("  -> 템플릿 내용: {}", res.getIndividualTemplateContent());
            log.info("  -> 버튼 제목: {}", res.getButtonTitle());
            log.info("  -> 생성일: {}", res.getCreatedAt());
            log.info("  -> 수정일: {}", res.getUpdatedAt());

            IndividualTemplateResponse asyncResult = future.get(2, TimeUnit.SECONDS);
            assertThat(asyncResult.getIndividualTemplateId()).isEqualTo(456);
            assertThat(asyncResult.getWorkspaceId()).isEqualTo(1);
            assertThat(asyncResult.getIndividualTemplateTitle()).isNull();
            assertThat(asyncResult.getIndividualTemplateContent()).isNull();
            assertThat(asyncResult.getButtonTitle()).isNull();
            assertThat(asyncResult.getCreatedAt()).isEqualTo(now);
            assertThat(asyncResult.getUpdatedAt()).isEqualTo(now);
            assertThat(asyncResult.isDeleted()).isFalse();

            verify(workspaceRepo, times(1)).findById(1);
            verify(individualTemplateRepo, times(1)).save(any(IndividualTemplate.class));
        }

        @Test
        @DisplayName("비동기에서도 workspaceId가 유효하지 않으면 즉시 예외가 발생한다(현재 구현 기준)")
        void createTemplateAsync_invalidWorkspace() {
            when(workspaceRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createTemplateAsync(999))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(workspaceRepo, times(1)).findById(999);
            verifyNoInteractions(individualTemplateRepo);
        }
    }

    // =========================
    // ===== READ 테스트들 =====
    // =========================
    @Nested
    @DisplayName("getAllTemplates")
    class GetAllTemplates {

        @Test
        @DisplayName("워크스페이스별 삭제되지 않은 템플릿 Page를 매핑하여 반환한다")
        void getAllTemplates_success() {
            when(workspaceMock.getWorkspaceId()).thenReturn(5);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e1 = createMockTemplate(10, "t1", "c1", "b1", workspaceMock, now, false);
            IndividualTemplate e2 = createMockTemplate(11, "t2", "c2", "b2", workspaceMock, now, false);

            Page<IndividualTemplate> repoPage = new PageImpl<>(List.of(e1, e2));

            when(individualTemplateRepo.findByWorkspace_WorkspaceIdAndIsDeletedFalse(anyInt(), any(Pageable.class)))
                    .thenReturn(repoPage);

            // when
            Page<IndividualTemplateResponse> page = service.getAllTemplates(5, PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent().get(0).getIndividualTemplateId()).isEqualTo(10);
            assertThat(page.getContent().get(0).getWorkspaceId()).isEqualTo(5);
            assertThat(page.getContent().get(0).isDeleted()).isFalse();
            assertThat(page.getContent().get(1).getIndividualTemplateId()).isEqualTo(11);
            assertThat(page.getContent().get(1).getWorkspaceId()).isEqualTo(5);

            verify(individualTemplateRepo, times(1))
                    .findByWorkspace_WorkspaceIdAndIsDeletedFalse(eq(5), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getAllTemplatesAsync")
    class GetAllTemplatesAsync {

        @Test
        @DisplayName("@Async로 Page 매핑 결과를 CompletableFuture로 반환한다")
        void getAllTemplatesAsync_success() throws Exception {
            when(workspaceMock.getWorkspaceId()).thenReturn(7);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e1 = createMockTemplate(20, null, null, null, workspaceMock, now, false);

            Page<IndividualTemplate> repoPage = new PageImpl<>(List.of(e1));

            when(individualTemplateRepo.findByWorkspace_WorkspaceIdAndIsDeletedFalse(anyInt(), any(Pageable.class)))
                    .thenReturn(repoPage);

            // when
            CompletableFuture<Page<IndividualTemplateResponse>> future =
                    service.getAllTemplatesAsync(7, PageRequest.of(0, 10));

            // then
            Page<IndividualTemplateResponse> page = future.get(2, TimeUnit.SECONDS);

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getIndividualTemplateId()).isEqualTo(20);
            assertThat(page.getContent().get(0).getWorkspaceId()).isEqualTo(7);
            assertThat(page.getContent().get(0).isDeleted()).isFalse();

            verify(individualTemplateRepo, times(1))
                    .findByWorkspace_WorkspaceIdAndIsDeletedFalse(eq(7), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getIndividualTemplateByStatus")
    class GetIndividualTemplateByStatus {

        @Test
        @DisplayName("상태별 템플릿을 조회한다 (DRAFT)")
        void getIndividualTemplateByStatus_draft() {
            when(workspaceMock.getWorkspaceId()).thenReturn(8);
            IndividualTemplate e1 = createMockTemplate(30, "draft-title", "draft-content", "draft-btn", workspaceMock, LocalDateTime.now(), false);
            Page<IndividualTemplate> repoPage = new PageImpl<>(List.of(e1));

            when(individualTemplateRepo.findByWorkspace_WorkspaceIdAndIsDeletedFalseAndStatus(
                    anyInt(), any(IndividualTemplate.Status.class), any(Pageable.class)))
                    .thenReturn(repoPage);

            // when
            Page<IndividualTemplateResponse> result = service.getIndividualTemplateByStatus(
                    8, IndividualTemplate.Status.DRAFT, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getIndividualTemplateId()).isEqualTo(30);
            assertThat(result.getContent().get(0).getWorkspaceId()).isEqualTo(8);

            verify(individualTemplateRepo).findByWorkspace_WorkspaceIdAndIsDeletedFalseAndStatus(
                    eq(8), eq(IndividualTemplate.Status.DRAFT), any(Pageable.class));
        }

        @Test
        @DisplayName("상태별 조회 - APPROVED")
        void getIndividualTemplateByStatus_approved() {
            when(workspaceMock.getWorkspaceId()).thenReturn(9);
            IndividualTemplate e1 = createMockTemplate(31, "approved-title", "approved-content", "approved-btn", workspaceMock, LocalDateTime.now(), false);
            Page<IndividualTemplate> repoPage = new PageImpl<>(List.of(e1));

            when(individualTemplateRepo.findByWorkspace_WorkspaceIdAndIsDeletedFalseAndStatus(
                    anyInt(), any(IndividualTemplate.Status.class), any(Pageable.class)))
                    .thenReturn(repoPage);

            // when
            Page<IndividualTemplateResponse> result = service.getIndividualTemplateByStatus(
                    9, IndividualTemplate.Status.APPROVED, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getIndividualTemplateId()).isEqualTo(31);
            assertThat(result.getContent().get(0).getWorkspaceId()).isEqualTo(9);

            verify(individualTemplateRepo).findByWorkspace_WorkspaceIdAndIsDeletedFalseAndStatus(
                    eq(9), eq(IndividualTemplate.Status.APPROVED), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getIndividualTemplateByStatusAsync")
    class GetIndividualTemplateByStatusAsync {

        @Test
        @DisplayName("@Async로 상태별 조회 결과를 CompletableFuture로 반환한다")
        void getIndividualTemplateByStatusAsync_success() throws Exception {
            when(workspaceMock.getWorkspaceId()).thenReturn(10);
            IndividualTemplate e1 = createMockTemplate(40, "async-title", "async-content", "async-btn", workspaceMock, LocalDateTime.now(), false);
            Page<IndividualTemplate> repoPage = new PageImpl<>(List.of(e1));

            when(individualTemplateRepo.findByWorkspace_WorkspaceIdAndIsDeletedFalseAndStatus(
                    anyInt(), any(IndividualTemplate.Status.class), any(Pageable.class)))
                    .thenReturn(repoPage);

            // when
            CompletableFuture<Page<IndividualTemplateResponse>> future = service.getIndividualTemplateByStatusAsync(
                    10, IndividualTemplate.Status.DRAFT, PageRequest.of(0, 10));

            // then
            Page<IndividualTemplateResponse> result = future.get(2, TimeUnit.SECONDS);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getIndividualTemplateId()).isEqualTo(40);
            assertThat(result.getContent().get(0).getWorkspaceId()).isEqualTo(10);

            verify(individualTemplateRepo).findByWorkspace_WorkspaceIdAndIsDeletedFalseAndStatus(
                    eq(10), eq(IndividualTemplate.Status.DRAFT), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getIndividualTemplate")
    class GetIndividualTemplate {

        @Test
        @DisplayName("개인 템플릿 단건을 매핑하여 반환한다")
        void getIndividualTemplate_success() {
            when(workspaceMock.getWorkspaceId()).thenReturn(3);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e = createMockTemplate(33, "title-33", "content-33", "button-33", workspaceMock, now, false);

            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(33, 3))
                    .thenReturn(Optional.of(e));

            // when
            IndividualTemplateResponse res = service.getIndividualTemplate(3, 33);

            // then
            assertThat(res.getIndividualTemplateId()).isEqualTo(33);
            assertThat(res.getWorkspaceId()).isEqualTo(3);
            assertThat(res.getIndividualTemplateTitle()).isEqualTo("title-33");
            assertThat(res.getIndividualTemplateContent()).isEqualTo("content-33");
            assertThat(res.getButtonTitle()).isEqualTo("button-33");
            assertThat(res.isDeleted()).isFalse();
            assertThat(res.getCreatedAt()).isEqualTo(now);
            assertThat(res.getUpdatedAt()).isEqualTo(now);

            verify(individualTemplateRepo, times(1))
                    .findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(33, 3);
        }

        @Test
        @DisplayName("조회 대상이 없으면 IllegalArgumentException을 던진다")
        void getIndividualTemplate_notFound() {
            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(99, 1))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getIndividualTemplate(1, 99))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 템플릿이 존재하지 않습니다");

            verify(individualTemplateRepo, times(1))
                    .findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(99, 1);
        }
    }

    @Nested
    @DisplayName("getIndividualTemplateAsync")
    class GetIndividualTemplateAsync {

        @Test
        @DisplayName("@Async로 단건 결과를 CompletableFuture로 반환한다")
        void getIndividualTemplateAsync_success() throws Exception {
            when(workspaceMock.getWorkspaceId()).thenReturn(4);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e = createMockTemplate(44, null, null, null, workspaceMock, now, false);

            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(44, 4))
                    .thenReturn(Optional.of(e));

            // when
            CompletableFuture<IndividualTemplateResponse> future =
                    service.getIndividualTemplateAsync(4, 44);

            // then
            IndividualTemplateResponse res = future.get(2, TimeUnit.SECONDS);

            assertThat(res.getIndividualTemplateId()).isEqualTo(44);
            assertThat(res.getWorkspaceId()).isEqualTo(4);
            assertThat(res.isDeleted()).isFalse();
            assertThat(res.getCreatedAt()).isEqualTo(now);
            assertThat(res.getUpdatedAt()).isEqualTo(now);

            verify(individualTemplateRepo, times(1))
                    .findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(44, 4);
        }

        @Test
        @DisplayName("비동기 단건 조회에서도 대상이 없으면 예외가 발생한다")
        void getIndividualTemplateAsync_notFound() {
            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(100, 5))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getIndividualTemplateAsync(5, 100))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(individualTemplateRepo, times(1))
                    .findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(100, 5);
        }
    }

    // Helper
    private IndividualTemplate createMockTemplate(Integer id, String title, String content, String buttonTitle,
                                                  Workspace workspace, LocalDateTime dateTime, boolean isDeleted) {
        IndividualTemplate mockEntity = mock(IndividualTemplate.class);
        when(mockEntity.getIndividualTemplateId()).thenReturn(id);
        when(mockEntity.getIndividualTemplateTitle()).thenReturn(title);
        when(mockEntity.getIndividualTemplateContent()).thenReturn(content);
        when(mockEntity.getButtonTitle()).thenReturn(buttonTitle);
        when(mockEntity.getWorkspace()).thenReturn(workspace);
        when(mockEntity.getCreatedAt()).thenReturn(dateTime);
        when(mockEntity.getUpdatedAt()).thenReturn(dateTime);
        when(mockEntity.getIsDeleted()).thenReturn(isDeleted);
        return mockEntity;
    }

    // SoftDelete
    @Test
    @DisplayName("updated=0 && 존재하지 않음 -> EntityNotFoundException (404)")
    void deleteTemplate_updated0_notExists_throw404() {
        // given
        Integer workspaceId = 10;
        Integer id = 999;
        when(individualTemplateRepo.softDeleteByIdAndWorkspace(id, workspaceId)).thenReturn(0);
        when(individualTemplateRepo.existsById(id)).thenReturn(false);

        // when & then
        assertThrows(EntityNotFoundException.class, () -> service.deleteTemplate(id, workspaceId));

        // verify
        verify(individualTemplateRepo).softDeleteByIdAndWorkspace(id, workspaceId);
        verify(individualTemplateRepo).existsById(id);
        verifyNoMoreInteractions(individualTemplateRepo);
    }

    @Test
    @DisplayName("updated=0 && 이미 삭제됨(existsById=true) -> 멱등 처리(예외 없음)")
    void deleteTemplate_updated0_alreadyDeleted_ok() {
        // given
        Integer workspaceId = 10;
        Integer id = 8;
        when(individualTemplateRepo.softDeleteByIdAndWorkspace(id, workspaceId)).thenReturn(0);
        when(individualTemplateRepo.existsById(id)).thenReturn(true);

        // when & then
        assertDoesNotThrow(() -> service.deleteTemplate(id, workspaceId));

        // verify
        verify(individualTemplateRepo).softDeleteByIdAndWorkspace(id, workspaceId);
        verify(individualTemplateRepo).existsById(id);
        verifyNoMoreInteractions(individualTemplateRepo);
    }
}
