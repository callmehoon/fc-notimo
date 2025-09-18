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
import org.mockito.ArgumentCaptor;
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
}