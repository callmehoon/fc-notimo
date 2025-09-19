package com.jober.final2teamdrhong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateCreateRequest;
import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateResponse;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;
import com.jober.final2teamdrhong.service.validator.WorkspaceValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class PublicTemplateServiceTest {

    @InjectMocks
    private PublicTemplateService publicTemplateService;

    @Mock
    private PublicTemplateRepository publicTemplateRepository;

    @Mock
    private IndividualTemplateRepository individualTemplateRepository;

    @Mock
    private WorkspaceValidator workspaceValidator;

    @Test
    @DisplayName("getTemplates는 Repository에서 조회한 페이지를 그대로 매핑해 반환한다")
    void getTemplates_ReturnsMappedPage() {
        // given
        PublicTemplate e1 = PublicTemplate.builder()
                .publicTemplateTitle("가나다")
                .publicTemplateContent("Content1")
                .buttonTitle("버튼1")
                .build();
        PublicTemplate e2 = PublicTemplate.builder()
                .publicTemplateTitle("나다라")
                .publicTemplateContent("Content2")
                .buttonTitle("버튼2")
                .build();

        Page<PublicTemplate> page = new PageImpl<>(List.of(e1, e2), PageRequest.of(0, 10, Sort.by("createdAt").descending()), 2);
        when(publicTemplateRepository.findAll(any(Pageable.class))).thenReturn(page);

        // when
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<PublicTemplateResponse> result = publicTemplateService.getTemplates(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(PublicTemplateResponse::publicTemplateTitle)
                .containsExactly("가나다", "나다라");
        verify(publicTemplateRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("createPublicTemplate는 개인 템플릿 값을 복사해 저장하고 응답을 반환한다")
    void createPublicTemplate_Success() {
        // given
        Integer individualTemplateId = 123;
        Integer userId = 1;

        // IndividualTemplate 및 관련 Workspace, User Mock 설정
        User mockUser = User.builder().userId(userId).build();
        Workspace mockWorkspace = Workspace.builder()
                .workspaceId(100)
                .user(mockUser)
                .workspaceName("Test Workspace")
                .workspaceUrl("testurl")
                .representerName("Test Representer")
                .representerPhoneNumber("010-1234-5678")
                .companyName("Test Company")
                .build();
        IndividualTemplate source = IndividualTemplate.builder()
                .individualTemplateTitle("원본 제목")
                .individualTemplateContent("원본 내용")
                .buttonTitle("원본 버튼")
                .workspace(mockWorkspace)
                .build();

        when(individualTemplateRepository.findByIdOrThrow(individualTemplateId)).thenReturn(source);

        // WorkspaceValidator Mock 설정: 소유권 검증 성공
        when(workspaceValidator.validateAndGetWorkspace(any(Integer.class), any(Integer.class)))
                .thenReturn(mockWorkspace);

        // save 호출 시 전달된 엔티티를 그대로 반환하도록 설정
        when(publicTemplateRepository.save(any(PublicTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        PublicTemplateResponse response = publicTemplateService
                .createPublicTemplate(new PublicTemplateCreateRequest(individualTemplateId), userId);

        // then
        assertThat(response.publicTemplateTitle()).isEqualTo("원본 제목");
        assertThat(response.publicTemplateContent()).isEqualTo("원본 내용");
        assertThat(response.buttonTitle()).isEqualTo("원본 버튼");

        // then: 저장된 엔티티 필드도 원본과 동일하게 복사되었는지 검증
        ArgumentCaptor<PublicTemplate> captor = ArgumentCaptor.forClass(PublicTemplate.class);
        verify(individualTemplateRepository).findByIdOrThrow(individualTemplateId);
        verify(workspaceValidator).validateAndGetWorkspace(mockWorkspace.getWorkspaceId(), userId);
        verify(publicTemplateRepository).save(captor.capture());
        PublicTemplate savedEntity = captor.getValue();
        assertThat(savedEntity.getPublicTemplateTitle()).isEqualTo("원본 제목");
        assertThat(savedEntity.getPublicTemplateContent()).isEqualTo("원본 내용");
        assertThat(savedEntity.getButtonTitle()).isEqualTo("원본 버튼");
    }

    @Test
    @DisplayName("createPublicTemplate는 개인 템플릿이 없으면 IllegalArgumentException을 던진다")
    void createPublicTemplate_NotFound() {
        // given
        Integer individualTemplateId = 999999;
        Integer userId = 1;
        when(individualTemplateRepository.findByIdOrThrow(individualTemplateId)).thenThrow(new IllegalArgumentException("해당 개인 템플릿을 찾을 수 없습니다."));

        // when & then
        assertThatThrownBy(() -> publicTemplateService
                .createPublicTemplate(new PublicTemplateCreateRequest(individualTemplateId), userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 개인 템플릿을 찾을 수 없습니다.");

        // 저장 로직은 호출되지 않아야 한다
        verify(publicTemplateRepository, never()).save(any(PublicTemplate.class));
        verify(workspaceValidator, never()).validateAndGetWorkspace(any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("createPublicTemplate는 개인 템플릿의 워크스페이스가 사용자의 소유가 아니면 IllegalArgumentException을 던진다")
    void createPublicTemplate_NotOwnedWorkspace_ThrowsException() {
        // given
        Integer individualTemplateId = 123;
        Integer userId = 1;
        Integer otherUserId = 2;

        User mockOtherUser = User.builder().userId(otherUserId).build();
        Workspace mockOtherWorkspace = Workspace.builder()
                .workspaceId(100)
                .user(mockOtherUser)
                .workspaceName("Other Test Workspace")
                .workspaceUrl("otherurl")
                .representerName("Other Representer")
                .representerPhoneNumber("010-8765-4321")
                .companyName("Other Test Company")
                .build();
        IndividualTemplate source = IndividualTemplate.builder()
                .individualTemplateTitle("원본 제목")
                .individualTemplateContent("원본 내용")
                .buttonTitle("원본 버튼")
                .workspace(mockOtherWorkspace)
                .build();

        when(individualTemplateRepository.findByIdOrThrow(individualTemplateId)).thenReturn(source);
        when(workspaceValidator.validateAndGetWorkspace(mockOtherWorkspace.getWorkspaceId(), userId))
                .thenThrow(new IllegalArgumentException("해당 워크스페이스에 대한 접근 권한이 없습니다."));

        // when & then
        assertThatThrownBy(() -> publicTemplateService
                .createPublicTemplate(new PublicTemplateCreateRequest(individualTemplateId), userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 워크스페이스에 대한 접근 권한이 없습니다.");

        verify(publicTemplateRepository, never()).save(any(PublicTemplate.class));
    }
}
