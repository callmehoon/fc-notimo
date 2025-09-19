package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndividualTemplateServiceTest {

    private static final Logger log = LoggerFactory.getLogger(IndividualTemplateServiceTest.class);

    @Mock
    private IndividualTemplateRepository individualTemplateRepo;

    @Mock
    private WorkspaceRepository workspaceRepo;

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
        @DisplayName("유효한 workspaceId면 빈 템플릿을 생성하고 응답을 반환한다")
        void createTemplate_success() {
            // given
            when(workspaceMock.getWorkspaceId()).thenReturn(1);
            when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspaceMock));

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate savedMock = mock(IndividualTemplate.class);
            when(savedMock.getIndividualTemplateId()).thenReturn(1);
            when(savedMock.getWorkspace()).thenReturn(workspaceMock);
            when(savedMock.getCreatedAt()).thenReturn(now);
            when(savedMock.getUpdatedAt()).thenReturn(now);

            when(individualTemplateRepo.save(any(IndividualTemplate.class))).thenReturn(savedMock);

            // when
            IndividualTemplateResponse res = service.createTemplate(1);

            // then
            assertThat(res).isNotNull();
            assertThat(res.getIndividualTemplateId()).isEqualTo(1);
            assertThat(res.getWorkspaceId()).isEqualTo(1);

            verify(workspaceRepo).findById(1);
            verify(individualTemplateRepo).save(any(IndividualTemplate.class));
        }

        @Test
        @DisplayName("존재하지 않는 workspaceId면 IllegalArgumentException 발생")
        void createTemplate_invalidWorkspace() {
            // given
            when(workspaceRepo.findById(999)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.createTemplate(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 workspaceId");
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
            when(workspaceRepo.findById(1)).thenReturn(Optional.of(workspaceMock));

            LocalDateTime now = LocalDateTime.now();
            IndividualTemplate savedMock = mock(IndividualTemplate.class);
            when(savedMock.getIndividualTemplateId()).thenReturn(456);
            when(savedMock.getWorkspace()).thenReturn(workspaceMock);
            when(savedMock.getCreatedAt()).thenReturn(now);
            when(savedMock.getUpdatedAt()).thenReturn(now);

            when(individualTemplateRepo.save(any(IndividualTemplate.class))).thenReturn(savedMock);

            // when
            CompletableFuture<IndividualTemplateResponse> future = service.createTemplateAsync(1);
            IndividualTemplateResponse res = future.get(2, TimeUnit.SECONDS);

            // then
            assertThat(res.getIndividualTemplateId()).isEqualTo(456);
            assertThat(res.getWorkspaceId()).isEqualTo(1);

            verify(workspaceRepo).findById(1);
            verify(individualTemplateRepo).save(any(IndividualTemplate.class));
        }

        @Test
        @DisplayName("유효하지 않은 workspaceId면 예외 발생")
        void createTemplateAsync_invalidWorkspace() {
            // given
            when(workspaceRepo.findById(999)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.createTemplateAsync(999))
                    .isInstanceOf(IllegalArgumentException.class);
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
            when(workspaceRepo.findByIdOrThrow(10)).thenReturn(workspaceMock);

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
            IndividualTemplateResponse res = service.createIndividualTemplateFromPublic(99, 10);

            // then
            assertThat(res.getIndividualTemplateId()).isEqualTo(1000);
            assertThat(res.getWorkspaceId()).isEqualTo(10);
            assertThat(res.getIndividualTemplateTitle()).isEqualTo("제목");
            assertThat(res.getIndividualTemplateContent()).isEqualTo("내용");
            assertThat(res.getButtonTitle()).isEqualTo("버튼");

            verify(publicTemplateRepo).findByIdOrThrow(99);
            verify(workspaceRepo).findByIdOrThrow(10);
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
            when(workspaceRepo.findByIdOrThrow(20)).thenReturn(workspaceMock);

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
                    service.createIndividualTemplateFromPublicAsync(200, 20);
            IndividualTemplateResponse res = future.get(2, TimeUnit.SECONDS);

            // then
            assertThat(res.getIndividualTemplateId()).isEqualTo(2000);
            assertThat(res.getWorkspaceId()).isEqualTo(20);
            assertThat(res.getIndividualTemplateTitle()).isEqualTo("Async제목");

            verify(publicTemplateRepo).findByIdOrThrow(200);
            verify(workspaceRepo).findByIdOrThrow(20);
            verify(individualTemplateRepo).save(any(IndividualTemplate.class));
        }
    }
}
