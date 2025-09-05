package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.entity.Favorite;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.FavoriteRepository;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FavoriteService 단위 테스트
 * ===== 테스트 메서드 순서 =====
 * 1. 개인 탬플릿 즐겨찾기 생성 성공
 * 2. 워크스페이스가 존재하지 않을 경우
 * 3. 개인 템플릿이 존재하지 않을 경우
 * 4. 이미 즐겨찾기로 등록했을 경우
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private IndividualTemplateRepository individualTemplateRepository;

    private Workspace mockWorkspace;
    private IndividualTemplate mockIndividualTemplate;
    private IndividualTemplateFavoriteRequest request;

    @BeforeEach
    void setUp() {
        request = new IndividualTemplateFavoriteRequest(1, 10);
        mockWorkspace = mock(Workspace.class);
        mockIndividualTemplate = mock(IndividualTemplate.class);
    }

    @Test
    @DisplayName("성공 : 개인 탬플릿 즐겨찾기 생성")
    void createIndividualTemplateFavorite_Success() {
        when(workspaceRepository.findById(1)).thenReturn(Optional.of(mockWorkspace));
        when(individualTemplateRepository.findById(10)).thenReturn(Optional.of(mockIndividualTemplate));
        when(favoriteRepository.findByWorkspaceAndIndividualTemplate(mockWorkspace, mockIndividualTemplate)).thenReturn(Optional.empty());

        favoriteService.createIndividualTemplateFavorite(request);

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }



    // 실패 테스트
    @Test
    @DisplayName("실패: 존재하지 않는 워크스페이스로 생성 시 예외 발생")
    void createFavorite_FailsWith_NonExistentWorkspace() {
        when(workspaceRepository.findById(request.getWorkspaceId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createIndividualTemplateFavorite(request));

        assertEquals("해당 워크스페이스를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 개인 템플릿으로 생성 시 예외 발생")
    void createFavorite_FailsWith_NonExistentTemplate() {
        when(workspaceRepository.findById(request.getWorkspaceId())).thenReturn(Optional.of(mockWorkspace));
        when(individualTemplateRepository.findById(request.getIndividualTemplateId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createIndividualTemplateFavorite(request));

        assertEquals("해당 템플릿을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 이미 존재하는 즐겨찾기 생성 시 예외 발생")
    void createFavorite_FailsWith_AlreadyExists() {
        when(workspaceRepository.findById(request.getWorkspaceId())).thenReturn(Optional.of(mockWorkspace));
        when(individualTemplateRepository.findById(request.getIndividualTemplateId())).thenReturn(Optional.of(mockIndividualTemplate));
        when(favoriteRepository.findByWorkspaceAndIndividualTemplate(mockWorkspace, mockIndividualTemplate))
                .thenReturn(Optional.of(mock(Favorite.class)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createIndividualTemplateFavorite(request));

        assertEquals("이미 즐겨찾기된 템플릿입니다.", exception.getMessage());
    }

}