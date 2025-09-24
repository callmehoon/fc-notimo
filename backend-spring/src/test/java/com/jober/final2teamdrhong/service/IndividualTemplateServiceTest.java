package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplatePageableRequest;
import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import com.jober.final2teamdrhong.service.validator.WorkspaceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class IndividualTemplateServiceTest {

    private static final Logger log = LoggerFactory.getLogger(IndividualTemplateServiceTest.class);

    @Mock
    private IndividualTemplateRepository individualTemplateRepo;

    @Mock
    private WorkspaceValidator workspaceValidator;

    @Mock
    private PublicTemplateRepository publicTemplateRepo;

    @InjectMocks
    private IndividualTemplateService service;

    private Workspace workspaceMock;

    @BeforeEach
    void setUp() {
        workspaceMock = mock(Workspace.class);
    }

    @Nested
    @DisplayName("createTemplate")
    class CreateTemplate {

        @Test
        @DisplayName("유효한 workspaceId면 빈 템플릿을 생성하고 응답을 반환한다")
        void createTemplate_success() {
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(1);
            when(workspaceValidator.validateAndGetWorkspace(1, 100))
                    .thenReturn(workspaceMock);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate savedMock = mock(IndividualTemplate.class);
            when(savedMock.getIndividualTemplateId()).thenReturn(1);
            when(savedMock.getWorkspace()).thenReturn(workspaceMock);
            when(savedMock.getCreatedAt()).thenReturn(now);
            when(savedMock.getUpdatedAt()).thenReturn(now);

            when(individualTemplateRepo.save(any(IndividualTemplate.class))).thenReturn(savedMock);

            // when
            IndividualTemplateResponse res = service.createTemplate(1, 100);

            // then
            assertThat(res).isNotNull();
            assertThat(res.getIndividualTemplateId()).isEqualTo(1);
            assertThat(res.getWorkspaceId()).isEqualTo(1);

            verify(workspaceValidator).validateAndGetWorkspace(1, 100);
            verify(individualTemplateRepo).save(any(IndividualTemplate.class));
        }

        @Test
        @DisplayName("존재하지 않는 workspaceId면 IllegalArgumentException 발생")
        void createTemplate_invalidWorkspace() {
            // given
            Integer workspaceId = 999;
            Integer userId = 100;

            when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                    .thenThrow(new IllegalArgumentException("유효하지 않은 workspaceId"));

            // when & then
            assertThatThrownBy(() -> service.createTemplate(workspaceId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 workspaceId");

            verify(workspaceValidator).validateAndGetWorkspace(workspaceId, userId);
            verifyNoInteractions(individualTemplateRepo);
        }

    }

    @Nested
    @DisplayName("createTemplateAsync")
    class CreateTemplateAsync {

        @Test
        @DisplayName("정상 실행 시 CompletableFuture로 결과를 반환한다")
        void createTemplateAsync_success() throws Exception {
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(1);
            when(workspaceValidator.validateAndGetWorkspace(1, 100))
                    .thenReturn(workspaceMock);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate savedMock = mock(IndividualTemplate.class);
            when(savedMock.getIndividualTemplateId()).thenReturn(456);
            when(savedMock.getWorkspace()).thenReturn(workspaceMock);
            when(savedMock.getCreatedAt()).thenReturn(now);
            when(savedMock.getUpdatedAt()).thenReturn(now);

            when(individualTemplateRepo.save(any(IndividualTemplate.class))).thenReturn(savedMock);

            // when
            CompletableFuture<IndividualTemplateResponse> future = service.createTemplateAsync(1, 100);
            IndividualTemplateResponse res = future.get(2, TimeUnit.SECONDS);

            // then
            assertThat(res.getIndividualTemplateId()).isEqualTo(456);
            assertThat(res.getWorkspaceId()).isEqualTo(1);

            verify(workspaceValidator).validateAndGetWorkspace(1, 100);
            verify(individualTemplateRepo).save(any(IndividualTemplate.class));
        }

        @Test
        @DisplayName("유효하지 않은 workspaceId면 예외 발생")
        void createTemplateAsync_invalidWorkspace() {
            // given
            Integer workspaceId = 999;
            Integer userId = 100;

            when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                    .thenThrow(new IllegalArgumentException("유효하지 않은 workspaceId"));

            // when & then
            assertThatThrownBy(() -> service.createTemplateAsync(workspaceId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 workspaceId");

            verify(workspaceValidator).validateAndGetWorkspace(workspaceId, userId);
            verifyNoInteractions(individualTemplateRepo);
        }
    }

    @Nested
    @DisplayName("createIndividualTemplateFromPublic")
    class CreateFromPublic {

        @Test
        @DisplayName("공용 템플릿 내용을 복사하여 개인 템플릿을 생성한다")
        void createFromPublic_success() {
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(10);
            when(workspaceValidator.validateAndGetWorkspace(10, 7)).thenReturn(workspaceMock);

            PublicTemplate publicMock = mock(PublicTemplate.class);
            when(publicMock.getPublicTemplateTitle()).thenReturn("제목");
            when(publicMock.getPublicTemplateContent()).thenReturn("내용");
            when(publicMock.getButtonTitle()).thenReturn("버튼");

            when(publicTemplateRepo.findByIdOrThrow(99)).thenReturn(publicMock);

            IndividualTemplate savedMock = mock(IndividualTemplate.class);
            when(savedMock.getIndividualTemplateId()).thenReturn(1000);
            when(savedMock.getIndividualTemplateTitle()).thenReturn("제목");
            when(savedMock.getIndividualTemplateContent()).thenReturn("내용");
            when(savedMock.getButtonTitle()).thenReturn("버튼");
            when(savedMock.getWorkspace()).thenReturn(workspaceMock);
            when(savedMock.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedMock.getUpdatedAt()).thenReturn(LocalDateTime.now());

            when(individualTemplateRepo.save(any(IndividualTemplate.class))).thenReturn(savedMock);

            // when
            IndividualTemplateResponse res = service.createIndividualTemplateFromPublic(99, 10, 7);

            // then
            assertThat(res.getIndividualTemplateId()).isEqualTo(1000);
            assertThat(res.getWorkspaceId()).isEqualTo(10);
            assertThat(res.getIndividualTemplateTitle()).isEqualTo("제목");
            assertThat(res.getIndividualTemplateContent()).isEqualTo("내용");
            assertThat(res.getButtonTitle()).isEqualTo("버튼");

            verify(publicTemplateRepo).findByIdOrThrow(99);
            verify(workspaceValidator).validateAndGetWorkspace(10, 7);
            verify(individualTemplateRepo).save(any(IndividualTemplate.class));
        }
    }

    @Nested
    @DisplayName("createIndividualTemplateFromPublicAsync")
    class CreateFromPublicAsync {

        @Test
        @DisplayName("비동기 실행도 정상적으로 PublicTemplate 기반 생성 결과를 반환한다")
        void createFromPublicAsync_success() throws Exception {
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(20);
            when(workspaceValidator.validateAndGetWorkspace(20, 8)).thenReturn(workspaceMock);

            PublicTemplate publicMock = mock(PublicTemplate.class);
            when(publicMock.getPublicTemplateTitle()).thenReturn("Async제목");
            when(publicMock.getPublicTemplateContent()).thenReturn("Async내용");
            when(publicMock.getButtonTitle()).thenReturn("Async버튼");

            when(publicTemplateRepo.findByIdOrThrow(200)).thenReturn(publicMock);

            IndividualTemplate savedMock = mock(IndividualTemplate.class);
            when(savedMock.getIndividualTemplateId()).thenReturn(2000);
            when(savedMock.getIndividualTemplateTitle()).thenReturn("Async제목");
            when(savedMock.getIndividualTemplateContent()).thenReturn("Async내용");
            when(savedMock.getButtonTitle()).thenReturn("Async버튼");
            when(savedMock.getWorkspace()).thenReturn(workspaceMock);
            when(savedMock.getCreatedAt()).thenReturn(LocalDateTime.now());
            when(savedMock.getUpdatedAt()).thenReturn(LocalDateTime.now());

            when(individualTemplateRepo.save(any(IndividualTemplate.class))).thenReturn(savedMock);

            // when
            CompletableFuture<IndividualTemplateResponse> future =
                    service.createIndividualTemplateFromPublicAsync(200, 20, 8);
            IndividualTemplateResponse res = future.get(2, TimeUnit.SECONDS);

            // then
            assertThat(res.getIndividualTemplateId()).isEqualTo(2000);
            assertThat(res.getWorkspaceId()).isEqualTo(20);
            assertThat(res.getIndividualTemplateTitle()).isEqualTo("Async제목");

            verify(publicTemplateRepo).findByIdOrThrow(200);
            verify(workspaceValidator).validateAndGetWorkspace(20, 8);
            verify(individualTemplateRepo).save(any(IndividualTemplate.class));
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

            when(individualTemplateRepo.findByWorkspace_WorkspaceId(anyInt(), any(Pageable.class)))
                    .thenReturn(repoPage);

            // given
            IndividualTemplatePageableRequest request = new IndividualTemplatePageableRequest();
            ReflectionTestUtils.setField(request, "page", 0);
            ReflectionTestUtils.setField(request, "size", 10);

            // when
            Page<IndividualTemplateResponse> page = service.getAllTemplates(5, 100, request);

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent().get(0).getIndividualTemplateId()).isEqualTo(10);
            assertThat(page.getContent().get(0).getWorkspaceId()).isEqualTo(5);
            assertThat(page.getContent().get(0).getIsDeleted()).isFalse();
            assertThat(page.getContent().get(1).getIndividualTemplateId()).isEqualTo(11);
            assertThat(page.getContent().get(1).getWorkspaceId()).isEqualTo(5);

            verify(individualTemplateRepo).findByWorkspace_WorkspaceId(eq(5), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getAllTemplatesAsync")
    class GetAllTemplatesAsync {

        @Test
        @DisplayName("@Async로 Page 매핑 결과를 CompletableFuture로 반환한다")
        void getAllTemplatesAsync_success() throws Exception {
            // given
            Integer workspaceId = 7;
            Integer userId = 100;

            when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                    .thenReturn(workspaceMock);
            when(workspaceMock.getWorkspaceId()).thenReturn(workspaceId);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e1 = createMockTemplate(20, null, null, null, workspaceMock, now, false);

            Page<IndividualTemplate> repoPage = new PageImpl<>(List.of(e1));
            when(individualTemplateRepo.findByWorkspace_WorkspaceId(eq(workspaceId), any(Pageable.class)))
                    .thenReturn(repoPage);

            IndividualTemplatePageableRequest request = new IndividualTemplatePageableRequest();
            ReflectionTestUtils.setField(request, "page", 0);
            ReflectionTestUtils.setField(request, "size", 10);

            // when
            CompletableFuture<Page<IndividualTemplateResponse>> future =
                    service.getAllTemplatesAsync(workspaceId, userId, request);

            // then
            Page<IndividualTemplateResponse> page = future.get(2, TimeUnit.SECONDS);

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getIndividualTemplateId()).isEqualTo(20);
            assertThat(page.getContent().get(0).getWorkspaceId()).isEqualTo(workspaceId);
            assertThat(page.getContent().get(0).getIsDeleted()).isFalse();

            verify(workspaceValidator).validateAndGetWorkspace(workspaceId, userId);
            verify(individualTemplateRepo).findByWorkspace_WorkspaceId(eq(workspaceId), any(Pageable.class));
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

            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceId(33, 3))
                    .thenReturn(Optional.of(e));

            // when
            IndividualTemplateResponse res = service.getIndividualTemplate(3, 100, 33);

            // then
            assertThat(res.getIndividualTemplateId()).isEqualTo(33);
            assertThat(res.getWorkspaceId()).isEqualTo(3);
            assertThat(res.getIndividualTemplateTitle()).isEqualTo("title-33");
            assertThat(res.getIndividualTemplateContent()).isEqualTo("content-33");
            assertThat(res.getButtonTitle()).isEqualTo("button-33");
            assertThat(res.getIsDeleted()).isFalse();
            assertThat(res.getCreatedAt()).isEqualTo(now);
            assertThat(res.getUpdatedAt()).isEqualTo(now);

            verify(individualTemplateRepo, times(1))
                    .findByIndividualTemplateIdAndWorkspace_WorkspaceId(33, 3);
        }

        @Test
        @DisplayName("조회 대상이 없으면 IllegalArgumentException을 던진다")
        void getIndividualTemplate_notFound() {
            // given
            Integer workspaceId = 1;
            Integer userId = 100;
            Integer templateId = 99;

            when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                    .thenReturn(workspaceMock); // 유효한 workspace 라고 가정
            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceId(templateId, workspaceId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getIndividualTemplate(workspaceId, userId, templateId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 템플릿이 존재하지 않습니다");

            verify(workspaceValidator).validateAndGetWorkspace(workspaceId, userId);
            verify(individualTemplateRepo, times(1))
                    .findByIndividualTemplateIdAndWorkspace_WorkspaceId(templateId, workspaceId);
        }
    }

    @Nested
    @DisplayName("getIndividualTemplateAsync")
    class GetIndividualTemplateAsync {

        @Test
        @DisplayName("@Async로 단건 결과를 CompletableFuture로 반환한다")
        void getIndividualTemplateAsync_success() throws Exception {
            // given
            Integer workspaceId = 4;
            Integer userId = 100;
            Integer templateId = 44;

            when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                    .thenReturn(workspaceMock);
            when(workspaceMock.getWorkspaceId()).thenReturn(workspaceId);

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate e = createMockTemplate(templateId, null, null, null, workspaceMock, now, false);

            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceId(templateId, workspaceId))
                    .thenReturn(Optional.of(e));

            // when
            CompletableFuture<IndividualTemplateResponse> future =
                    service.getIndividualTemplateAsync(workspaceId, userId, templateId);

            // then
            IndividualTemplateResponse res = future.get(2, TimeUnit.SECONDS);

            assertThat(res.getIndividualTemplateId()).isEqualTo(templateId);
            assertThat(res.getWorkspaceId()).isEqualTo(workspaceId);
            assertThat(res.getIsDeleted()).isFalse();
            assertThat(res.getCreatedAt()).isEqualTo(now);
            assertThat(res.getUpdatedAt()).isEqualTo(now);

            verify(workspaceValidator).validateAndGetWorkspace(workspaceId, userId);
            verify(individualTemplateRepo).findByIndividualTemplateIdAndWorkspace_WorkspaceId(templateId, workspaceId);
        }

        @Test
        @DisplayName("비동기 단건 조회에서도 대상이 없으면 예외가 발생한다")
        void getIndividualTemplateAsync_notFound() {
            // given
            Integer workspaceId = 5;
            Integer userId = 200;
            Integer templateId = 100;

            when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                    .thenReturn(workspaceMock);
            when(individualTemplateRepo.findByIndividualTemplateIdAndWorkspace_WorkspaceId(templateId, workspaceId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getIndividualTemplateAsync(workspaceId, userId, templateId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 템플릿이 존재하지 않습니다");

            verify(workspaceValidator).validateAndGetWorkspace(workspaceId, userId);
            verify(individualTemplateRepo).findByIndividualTemplateIdAndWorkspace_WorkspaceId(templateId, workspaceId);
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
    @Nested
    @DisplayName("deleteTemplate")
    class DeleteTemplate {

        @Test
        @DisplayName("존재하는 템플릿을 삭제하면 softDelete가 호출되고 저장된다")
        void deleteTemplate_success() {
            // given
            Integer workspaceId = 10;
            Integer id = 1;
            Integer userId = 100;

            // workspaceValidator는 실제로 호출되므로 mock 필요
            when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                    .thenReturn(workspaceMock);

            IndividualTemplate templateMock = mock(IndividualTemplate.class);
            when(individualTemplateRepo.findById(id)).thenReturn(Optional.of(templateMock));
            when(individualTemplateRepo.save(templateMock)).thenReturn(templateMock);

            // when
            assertDoesNotThrow(() -> service.deleteTemplate(id, workspaceId, userId));

            // then
            verify(workspaceValidator).validateAndGetWorkspace(workspaceId, userId);
            verify(individualTemplateRepo).findById(id);
            verify(templateMock).softDelete();
            verify(individualTemplateRepo).save(templateMock);
        }


        @Test
        @DisplayName("없는 템플릿 ID면 EntityNotFoundException 발생")
        void deleteTemplate_notFound_throw404() {
            // given
            Integer workspaceId = 10;
            Integer userId = 100;
            Integer missingId = 999;

            when(workspaceValidator.validateAndGetWorkspace(workspaceId, userId))
                    .thenReturn(workspaceMock);
            when(individualTemplateRepo.findById(missingId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(EntityNotFoundException.class,
                    () -> service.deleteTemplate(missingId, workspaceId, userId));

            verify(workspaceValidator).validateAndGetWorkspace(workspaceId, userId);
            verify(individualTemplateRepo).findById(missingId);
            verifyNoMoreInteractions(individualTemplateRepo);
        }

        @Test
        @DisplayName("템플릿은 있지만 다른 workspaceId에 속하면 IllegalArgumentException 발생")
        void deleteTemplate_wrongWorkspace_throw400() {
            // given
            Integer wrongWorkspaceId = 99;
            Integer userId = 200;
            Integer id = 5;

            when(workspaceValidator.validateAndGetWorkspace(wrongWorkspaceId, userId))
                    .thenThrow(new IllegalArgumentException("워크스페이스를 찾을 수 없거나 접근권한이 없습니다. ID: " + wrongWorkspaceId));

            // when & then
            assertThrows(IllegalArgumentException.class,
                    () -> service.deleteTemplate(id, wrongWorkspaceId, userId));

            verify(workspaceValidator).validateAndGetWorkspace(wrongWorkspaceId, userId);
            verifyNoMoreInteractions(individualTemplateRepo);
        }

    }

}
