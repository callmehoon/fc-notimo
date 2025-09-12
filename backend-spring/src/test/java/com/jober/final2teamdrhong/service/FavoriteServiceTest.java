package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.favorite.FavoritePageRequest;
import com.jober.final2teamdrhong.dto.favorite.FavoriteResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
     */
    @Test
    @DisplayName("성공 : 개인 탬플릿 즐겨찾기 생성")
    void createIndividualTemplateFavorite_Success() {
        when(workspaceRepository.findByIdOrThrow(1)).thenReturn(mockWorkspace);
        when(individualTemplateRepository.findByIdOrThrow(10)).thenReturn(mockIndividualTemplate);
        doNothing().when(favoriteRepository).validateIndividualTemplateNotExists(mockWorkspace, mockIndividualTemplate);

        favoriteService.createIndividualTemplateFavorite(mockIndividualTemplateFavoriteRequest);

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 워크스페이스로 생성 시 예외 발생")
    void createFavorite_FailsWith_NonExistentWorkspace() {
        when(workspaceRepository.findByIdOrThrow(mockIndividualTemplateFavoriteRequest.getWorkspaceId()))
                .thenThrow(new IllegalArgumentException("해당 워크스페이스를 찾을 수 없습니다."));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createIndividualTemplateFavorite(mockIndividualTemplateFavoriteRequest));

        assertEquals("해당 워크스페이스를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 개인 템플릿으로 생성 시 예외 발생")
    void createFavorite_FailsWith_NonExistentTemplate() {
        when(workspaceRepository.findByIdOrThrow(mockIndividualTemplateFavoriteRequest.getWorkspaceId())).thenReturn(mockWorkspace);
        when(individualTemplateRepository.findByIdOrThrow(mockIndividualTemplateFavoriteRequest.getIndividualTemplateId()))
                .thenThrow(new IllegalArgumentException("해당 개인 템플릿을 찾을 수 없습니다."));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createIndividualTemplateFavorite(mockIndividualTemplateFavoriteRequest));

        assertEquals("해당 개인 템플릿을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 이미 존재하는 즐겨찾기 생성 시 예외 발생")
    void createFavorite_FailsWith_AlreadyExists() {
        when(workspaceRepository.findByIdOrThrow(mockIndividualTemplateFavoriteRequest.getWorkspaceId())).thenReturn(mockWorkspace);
        when(individualTemplateRepository.findByIdOrThrow(mockIndividualTemplateFavoriteRequest.getIndividualTemplateId())).thenReturn(mockIndividualTemplate);
        doThrow(new IllegalArgumentException("이미 즐겨찾기된 개인 템플릿입니다."))
                .when(favoriteRepository).validateIndividualTemplateNotExists(mockWorkspace, mockIndividualTemplate);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createIndividualTemplateFavorite(mockIndividualTemplateFavoriteRequest));

        assertEquals("이미 즐겨찾기된 개인 템플릿입니다.", exception.getMessage());
    }



    /**
     * FavoriteService 공용 템플릿 생성 기능 단위 테스트
     */
    @Test
    @DisplayName("성공 : 공용 템플릿 즐겨찾기 생성")
    void createPublicTemplateFavorite_Success() {
        when(workspaceRepository.findByIdOrThrow(1)).thenReturn(mockWorkspace);
        when(publicTemplateRepository.findByIdOrThrow(100)).thenReturn(mockPublicTemplate);
        doNothing().when(favoriteRepository).validatePublicTemplateNotExists(mockWorkspace, mockPublicTemplate);

        favoriteService.createPublicTemplateFavorite(mockPublicTemplateFavoriteRequest);

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 워크스페이스로 공용 즐겨찾기 생성 시 예외 발생")
    void createPublicFavorite_FailsWith_NonExistentWorkspace() {
        when(workspaceRepository.findByIdOrThrow(mockPublicTemplateFavoriteRequest.getWorkspaceId()))
                .thenThrow(new IllegalArgumentException("해당 워크스페이스를 찾을 수 없습니다."));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createPublicTemplateFavorite(mockPublicTemplateFavoriteRequest));

        assertEquals("해당 워크스페이스를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 공용 템플릿으로 생성 시 예외 발생")
    void createPublicFavorite_FailsWith_NonExistentTemplate() {
        when(workspaceRepository.findByIdOrThrow(mockPublicTemplateFavoriteRequest.getWorkspaceId())).thenReturn(mockWorkspace);
        when(publicTemplateRepository.findByIdOrThrow(mockPublicTemplateFavoriteRequest.getPublicTemplateId()))
                .thenThrow(new IllegalArgumentException("해당 공용 템플릿을 찾을 수 없습니다."));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createPublicTemplateFavorite(mockPublicTemplateFavoriteRequest));

        assertEquals("해당 공용 템플릿을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("실패 : 이미 존재하는 공용 즐겨찾기 생성 시 예외 발생")
    void createPublicFavorite_FailsWith_AlreadyExists() {
        when(workspaceRepository.findByIdOrThrow(mockPublicTemplateFavoriteRequest.getWorkspaceId())).thenReturn(mockWorkspace);
        when(publicTemplateRepository.findByIdOrThrow(mockPublicTemplateFavoriteRequest.getPublicTemplateId())).thenReturn(mockPublicTemplate);
        doThrow(new IllegalArgumentException("이미 즐겨찾기된 공용 템플릿입니다."))
                .when(favoriteRepository).validatePublicTemplateNotExists(mockWorkspace, mockPublicTemplate);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.createPublicTemplateFavorite(mockPublicTemplateFavoriteRequest));

        assertEquals("이미 즐겨찾기된 공용 템플릿입니다.", exception.getMessage());
    }





    // ====================== Read ======================
    /**
     * FavoriteService 즐겨찾기 조회 기능 단위 테스트
     */
    @Test
    @DisplayName("성공(단위): 즐겨찾기 전체 목록 페이징 조회")
    void getFavorites_withoutTemplateType_shouldReturnPage() {
        // given
        Integer workspaceId = 1;
        FavoritePageRequest pageRequest = new FavoritePageRequest();
        Workspace workspace = mock(Workspace.class);

        PublicTemplate publicTemplate = mock(PublicTemplate.class);
        when(publicTemplate.getPublicTemplateId()).thenReturn(100);
        when(publicTemplate.getPublicTemplateTitle()).thenReturn("공용 제목");

        IndividualTemplate individualTemplate = mock(IndividualTemplate.class);
        when(individualTemplate.getIndividualTemplateId()).thenReturn(10);
        when(individualTemplate.getIndividualTemplateTitle()).thenReturn("개인 제목");

        Favorite publicFavorite = Favorite.builder().workspace(workspace).publicTemplate(publicTemplate).build();
        Favorite individualFavorite = Favorite.builder().workspace(workspace).individualTemplate(individualTemplate).build();
        Page<Favorite> mockPage = new PageImpl<>(List.of(publicFavorite, individualFavorite));

        when(workspaceRepository.findByIdOrThrow(workspaceId)).thenReturn(workspace);
        when(favoriteRepository.findFavorites(eq(workspace), eq(null), any(Pageable.class))).thenReturn(mockPage);

        // when
        Page<FavoriteResponse> result = favoriteService.getFavoritesByWorkspace(workspaceId, null, pageRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().size()).isEqualTo(2);
        verify(favoriteRepository).findFavorites(eq(workspace), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("성공(단위): 즐겨찾기 목록 페이징 조회 (public) - 반환된 내용 검증")
    void getFavorites_withPublicTemplateType_shouldReturnPage() {
        // given
        Integer workspaceId = 1;
        FavoritePageRequest pageRequest = new FavoritePageRequest();
        Workspace workspace = mock(Workspace.class);

        PublicTemplate publicTemplate = mock(PublicTemplate.class);
        when(publicTemplate.getPublicTemplateId()).thenReturn(100);
        when(publicTemplate.getPublicTemplateTitle()).thenReturn("공용 제목");

        Favorite publicFavorite = Favorite.builder().workspace(workspace).publicTemplate(publicTemplate).build();
        Page<Favorite> mockPage = new PageImpl<>(List.of(publicFavorite));

        when(workspaceRepository.findByIdOrThrow(workspaceId)).thenReturn(workspace);
        when(favoriteRepository.findFavorites(eq(workspace), eq(Favorite.TemplateType.PUBLIC), any(Pageable.class))).thenReturn(mockPage);

        // when
        Page<FavoriteResponse> result = favoriteService.getFavoritesByWorkspace(workspaceId, Favorite.TemplateType.PUBLIC, pageRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        FavoriteResponse response = result.getContent().get(0);
        assertThat(response.getTemplateType()).isEqualTo("PUBLIC");
        assertThat(response.getTemplateId()).isEqualTo(publicTemplate.getPublicTemplateId());
        assertThat(response.getTemplateTitle()).isEqualTo(publicTemplate.getPublicTemplateTitle());
        verify(favoriteRepository).findFavorites(eq(workspace), eq(Favorite.TemplateType.PUBLIC), any(Pageable.class));
    }

    @Test
    @DisplayName("성공(단위): 즐겨찾기 목록 페이징 조회 (individual) - 반환된 내용 검증")
    void getFavorites_withIndividualTemplateType_shouldReturnPage() {
        // given
        Integer workspaceId = 1;
        FavoritePageRequest pageRequest = new FavoritePageRequest();
        Workspace workspace = mock(Workspace.class);

        IndividualTemplate individualTemplate = mock(IndividualTemplate.class);
        when(individualTemplate.getIndividualTemplateId()).thenReturn(10);
        when(individualTemplate.getIndividualTemplateTitle()).thenReturn("개인 제목");

        Favorite individualFavorite = Favorite.builder().workspace(workspace).individualTemplate(individualTemplate).build();
        Page<Favorite> mockPage = new PageImpl<>(List.of(individualFavorite));

        when(workspaceRepository.findByIdOrThrow(workspaceId)).thenReturn(workspace);
        when(favoriteRepository.findFavorites(eq(workspace), eq(Favorite.TemplateType.INDIVIDUAL), any(Pageable.class))).thenReturn(mockPage);

        // when
        Page<FavoriteResponse> result = favoriteService.getFavoritesByWorkspace(workspaceId, Favorite.TemplateType.INDIVIDUAL, pageRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        FavoriteResponse response = result.getContent().get(0);
        assertThat(response.getTemplateType()).isEqualTo("INDIVIDUAL");
        assertThat(response.getTemplateId()).isEqualTo(individualTemplate.getIndividualTemplateId());
        assertThat(response.getTemplateTitle()).isEqualTo(individualTemplate.getIndividualTemplateTitle());
        verify(favoriteRepository).findFavorites(eq(workspace), eq(Favorite.TemplateType.INDIVIDUAL), any(Pageable.class));
    }




    // ====================== Delete ======================
    /**
     * FavoriteService 즐겨찾기 삭제 기능 단위 테스트
     */
    @Test
    @DisplayName("성공(단위): 즐겨찾기 삭제")
    void deleteFavorite_Success() {
        // given
        Integer favoriteId = 1;
        Favorite mockFavorite = mock(Favorite.class);
        when(favoriteRepository.findById(favoriteId)).thenReturn(Optional.of(mockFavorite));
        doNothing().when(favoriteRepository).delete(mockFavorite);

        // when
        favoriteService.deleteFavorite(favoriteId);

        // then
        verify(favoriteRepository, times(1)).findById(favoriteId);
        verify(favoriteRepository, times(1)).delete(mockFavorite);
    }

    @Test
    @DisplayName("실패(단위): 존재하지 않는 즐겨찾기 삭제 시 예외 발생")
    void deleteFavorite_FailsWith_NotFound() {
        // given
        Integer favoriteId = 999;
        when(favoriteRepository.findById(favoriteId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> favoriteService.deleteFavorite(favoriteId));

        assertEquals("해당 즐겨찾기를 찾을 수 없습니다.", exception.getMessage());
        verify(favoriteRepository, never()).delete(any());
    }
}
