package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.entity.Favorite;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.FavoriteRepository;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;
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
    @Mock
    private PublicTemplateRepository publicTemplateRepository;

    private Workspace mockWorkspace;
    private IndividualTemplate mockIndividualTemplate;
    private IndividualTemplateFavoriteRequest mockIndividualTemplateFavoriteRequest;
    private PublicTemplate mockPublicTemplate;
    private PublicTemplateFavoriteRequest mockPublicTemplateFavoriteRequest;

    @BeforeEach
    void setUp() {
        mockWorkspace = mock(Workspace.class);
        // 개인 템플릿 테스트 데이터
        mockIndividualTemplateFavoriteRequest = new IndividualTemplateFavoriteRequest(1, 10);
        mockIndividualTemplate = mock(IndividualTemplate.class);

        // 공용 템플릿 테스트 데이터
        mockPublicTemplateFavoriteRequest = new PublicTemplateFavoriteRequest(1, 100);
        mockPublicTemplate = mock(PublicTemplate.class);
    }

    /**
     * FavoriteService 개인 템플릿 생성 기능 단위 테스트
     * ===== 테스트 메서드 순서 =====
     * 1. 개인 탬플릿 즐겨찾기 생성(성공)
     * 2. 워크스페이스가 존재하지 않을 경우(실패)
     * 3. 개인 템플릿이 존재하지 않을 경우(실패)
     * 4. 이미 즐겨찾기로 등록했을 경우(실패)
     */
    @Test
    @DisplayName("성공 : 개인 탬플릿 즐겨찾기 생성")
    void createIndividualTemplateFavorite_Success() {
        when(workspaceRepository.findById(1)).thenReturn(Optional.of(mockWorkspace));
        when(individualTemplateRepository.findById(10)).thenReturn(Optional.of(mockIndividualTemplate));
        when(favoriteRepository.findByWorkspaceAndIndividualTemplate(mockWorkspace, mockIndividualTemplate)).thenReturn(Optional.empty());

        favoriteService.createIndividualTemplateFavorite(mockIndividualTemplateFavoriteRequest);

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 워크스페이스로 생성 시 예외 발생")
    void createFavorite_FailsWith_NonExistentWorkspace() {
        when(workspaceRepository.findById(mockIndividualTemplateFavoriteRequest.getWorkspaceId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createIndividualTemplateFavorite(mockIndividualTemplateFavoriteRequest));

        assertEquals("해당 워크스페이스를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 개인 템플릿으로 생성 시 예외 발생")
    void createFavorite_FailsWith_NonExistentTemplate() {
        when(workspaceRepository.findById(mockIndividualTemplateFavoriteRequest.getWorkspaceId())).thenReturn(Optional.of(mockWorkspace));
        when(individualTemplateRepository.findById(mockIndividualTemplateFavoriteRequest.getIndividualTemplateId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createIndividualTemplateFavorite(mockIndividualTemplateFavoriteRequest));

        assertEquals("해당 개인 템플릿을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 이미 존재하는 즐겨찾기 생성 시 예외 발생")
    void createFavorite_FailsWith_AlreadyExists() {
        when(workspaceRepository.findById(mockIndividualTemplateFavoriteRequest.getWorkspaceId())).thenReturn(Optional.of(mockWorkspace));
        when(individualTemplateRepository.findById(mockIndividualTemplateFavoriteRequest.getIndividualTemplateId())).thenReturn(Optional.of(mockIndividualTemplate));
        when(favoriteRepository.findByWorkspaceAndIndividualTemplate(mockWorkspace, mockIndividualTemplate))
                .thenReturn(Optional.of(mock(Favorite.class)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createIndividualTemplateFavorite(mockIndividualTemplateFavoriteRequest));

        assertEquals("이미 즐겨찾기된 개인 템플릿입니다.", exception.getMessage());
    }



    /**
     * FavoriteService 공용 템플릿 생성 기능 단위 테스트
     * ===== 테스트 메서드 순서 =====
     * 1. 공용 탬플릿 즐겨찾기 생성(성공)
     * 2. 워크스페이스가 존재하지 않을 경우(실패)
     * 3. 공용 템플릿이 존재하지 않을 경우(실패)
     * 4. 이미 즐겨찾기로 등록했을 경우(실패)
     */
    @Test
    @DisplayName("성공 : 공용 템플릿 즐겨찾기 생성")
    void createPublicTemplateFavorite_Success() {
        when(workspaceRepository.findById(1)).thenReturn(Optional.of(mockWorkspace));
        when(publicTemplateRepository.findById(100)).thenReturn(Optional.of(mockPublicTemplate));
        when(favoriteRepository.findByWorkspaceAndPublicTemplate(mockWorkspace, mockPublicTemplate)).thenReturn(Optional.empty());

        favoriteService.createPublicTemplateFavorite(mockPublicTemplateFavoriteRequest);

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 워크스페이스로 공용 즐겨찾기 생성 시 예외 발생")
    void createPublicFavorite_FailsWith_NonExistentWorkspace() {
        when(workspaceRepository.findById(mockPublicTemplateFavoriteRequest.getWorkspaceId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createPublicTemplateFavorite(mockPublicTemplateFavoriteRequest));

        assertEquals("해당 워크스페이스를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 공용 템플릿으로 생성 시 예외 발생")
    void createPublicFavorite_FailsWith_NonExistentTemplate() {
        when(workspaceRepository.findById(mockPublicTemplateFavoriteRequest.getWorkspaceId())).thenReturn(Optional.of(mockWorkspace));
        when(publicTemplateRepository.findById(mockPublicTemplateFavoriteRequest.getPublicTemplateId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createPublicTemplateFavorite(mockPublicTemplateFavoriteRequest));

        assertEquals("해당 공용 템플릿을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 이미 존재하는 공용 즐겨찾기 생성 시 예외 발생")
    void createPublicFavorite_FailsWith_AlreadyExists() {
        when(workspaceRepository.findById(mockPublicTemplateFavoriteRequest.getWorkspaceId())).thenReturn(Optional.of(mockWorkspace));
        when(publicTemplateRepository.findById(mockPublicTemplateFavoriteRequest.getPublicTemplateId())).thenReturn(Optional.of(mockPublicTemplate));
        when(favoriteRepository.findByWorkspaceAndPublicTemplate(mockWorkspace, mockPublicTemplate))
                .thenReturn(Optional.of(mock(Favorite.class)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createPublicTemplateFavorite(mockPublicTemplateFavoriteRequest));

        assertEquals("이미 즐겨찾기된 공용 템플릿입니다.", exception.getMessage());
    }

}
