package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

            when(individualTemplateRepo.save(any(IndividualTemplate.class))).thenReturn(savedMock);

            // when
            CompletableFuture<IndividualTemplateResponse> future = service.createTemplateAsync(1);

            log.info("[비동기] 생성된 IndividualTemplateResponse 정보:");
            log.info("  -> IndividualTemplate ID: {}", future.get().getIndividualTemplateId());
            log.info("  -> Workspace ID: {}", future.get().getWorkspaceId());
            log.info("  -> 템플릿 제목: {}", future.get().getIndividualTemplateTitle());
            log.info("  -> 템플릿 내용: {}", future.get().getIndividualTemplateContent());
            log.info("  -> 버튼 제목: {}", future.get().getButtonTitle());
            log.info("  -> 생성일: {}", future.get().getCreatedAt());
            log.info("  -> 수정일: {}", future.get().getUpdatedAt());

            // then
            IndividualTemplateResponse res = future.get(2, TimeUnit.SECONDS);
            assertThat(res.getIndividualTemplateId()).isEqualTo(456);
            assertThat(res.getWorkspaceId()).isEqualTo(1);
            assertThat(res.getIndividualTemplateTitle()).isNull();
            assertThat(res.getIndividualTemplateContent()).isNull();
            assertThat(res.getButtonTitle()).isNull();
            assertThat(res.getCreatedAt()).isEqualTo(now);
            assertThat(res.getUpdatedAt()).isEqualTo(now);

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
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(5);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e1 = mock(IndividualTemplate.class);
            when(e1.getIndividualTemplateId()).thenReturn(10);
            when(e1.getIndividualTemplateTitle()).thenReturn("t1");
            when(e1.getIndividualTemplateContent()).thenReturn("c1");
            when(e1.getButtonTitle()).thenReturn("b1");
            when(e1.getWorkspace()).thenReturn(workspaceMock);
            when(e1.getCreatedAt()).thenReturn(now);
            when(e1.getUpdatedAt()).thenReturn(now);
            when(e1.isDeleted()).thenReturn(false);

            IndividualTemplate e2 = mock(IndividualTemplate.class);
            when(e2.getIndividualTemplateId()).thenReturn(11);
            when(e2.getIndividualTemplateTitle()).thenReturn("t2");
            when(e2.getIndividualTemplateContent()).thenReturn("c2");
            when(e2.getButtonTitle()).thenReturn("b2");
            when(e2.getWorkspace()).thenReturn(workspaceMock);
            when(e2.getCreatedAt()).thenReturn(now);
            when(e2.getUpdatedAt()).thenReturn(now);
            when(e2.isDeleted()).thenReturn(false);

            Page<IndividualTemplate> repoPage = new PageImpl<>(List.of(e1, e2));

            when(individualTemplateRepo.findByWorkspace_WorkspaceIdAndIsDeletedFalse(anyInt(), any(Pageable.class)))
                    .thenReturn(repoPage);

            // when
            Page<IndividualTemplateResponse> page = service.getAllTemplates(5, Pageable.unpaged());

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
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(7);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e1 = mock(IndividualTemplate.class);
            when(e1.getIndividualTemplateId()).thenReturn(20);
            when(e1.getIndividualTemplateTitle()).thenReturn(null);
            when(e1.getIndividualTemplateContent()).thenReturn(null);
            when(e1.getButtonTitle()).thenReturn(null);
            when(e1.getWorkspace()).thenReturn(workspaceMock);
            when(e1.getCreatedAt()).thenReturn(now);
            when(e1.getUpdatedAt()).thenReturn(now);
            when(e1.isDeleted()).thenReturn(false);

            Page<IndividualTemplate> repoPage = new PageImpl<>(List.of(e1));

            when(individualTemplateRepo.findByWorkspace_WorkspaceIdAndIsDeletedFalse(anyInt(), any(Pageable.class)))
                    .thenReturn(repoPage);

            // when
            CompletableFuture<Page<IndividualTemplateResponse>> future =
                    service.getAllTemplatesAsync(7, Pageable.unpaged());

            Page<IndividualTemplateResponse> page = future.get(2, TimeUnit.SECONDS);

            // then
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getIndividualTemplateId()).isEqualTo(20);
            assertThat(page.getContent().get(0).getWorkspaceId()).isEqualTo(7);
            assertThat(page.getContent().get(0).isDeleted()).isFalse();

            verify(individualTemplateRepo, times(1))
                    .findByWorkspace_WorkspaceIdAndIsDeletedFalse(eq(7), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getIndividualTemplate")
    class GetIndividualTemplate {

        @Test
        @DisplayName("개인 템플릿 단건을 매핑하여 반환한다")
        void getIndividualTemplate_success() {
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(3);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e = mock(IndividualTemplate.class);
            when(e.getIndividualTemplateId()).thenReturn(33);
            when(e.getIndividualTemplateTitle()).thenReturn("title-33");
            when(e.getIndividualTemplateContent()).thenReturn("content-33");
            when(e.getButtonTitle()).thenReturn("button-33");
            when(e.getWorkspace()).thenReturn(workspaceMock);
            when(e.getCreatedAt()).thenReturn(now);
            when(e.getUpdatedAt()).thenReturn(now);
            when(e.isDeleted()).thenReturn(false);

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
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(4);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e = mock(IndividualTemplate.class);
            when(e.getIndividualTemplateId()).thenReturn(44);
            when(e.getIndividualTemplateTitle()).thenReturn(null);
            when(e.getIndividualTemplateContent()).thenReturn(null);
            when(e.getButtonTitle()).thenReturn(null);
            when(e.getWorkspace()).thenReturn(workspaceMock);
            when(e.getCreatedAt()).thenReturn(now);
            when(e.getUpdatedAt()).thenReturn(now);
            when(e.isDeleted()).thenReturn(false);

            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(44, 4))
                    .thenReturn(Optional.of(e));

            // when
            CompletableFuture<IndividualTemplateResponse> future =
                    service.getIndividualTemplateAsync(4, 44);

            IndividualTemplateResponse res = future.get(2, TimeUnit.SECONDS);

            // then
            assertThat(res.getIndividualTemplateId()).isEqualTo(44);
            assertThat(res.getWorkspaceId()).isEqualTo(4);
            assertThat(res.isDeleted()).isFalse();
            assertThat(res.getCreatedAt()).isEqualTo(now);
            assertThat(res.getUpdatedAt()).isEqualTo(now);

            verify(individualTemplateRepo, times(1))
                    .findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(44, 4);
        }

        @Test
        @DisplayName("비동기 단건 조회에서도 대상이 없으면 예외가 발생한다(현재 구현 기준)")
        void getIndividualTemplateAsync_notFound() {
            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(100, 5))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getIndividualTemplateAsync(5, 100))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(individualTemplateRepo, times(1))
                    .findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(100, 5);
        }
    }
}
