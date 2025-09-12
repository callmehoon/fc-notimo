package com.jober.final2teamdrhong.service;

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
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

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





    // ====================== Read ======================
    /**
     * FavoriteService 즐겨찾기 조회 기능 단위 테스트
     */
    @Test
    @DisplayName("성공(단위): 즐겨찾기 전체 목록 조회 (페이징 없음)")
    void getFavorites_withoutTemplateType_shouldReturnList() {
        // given
        Integer workspaceId = 1;
        Workspace workspace = mock(Workspace.class);

        PublicTemplate publicTemplate = mock(PublicTemplate.class);
        when(publicTemplate.getPublicTemplateId()).thenReturn(100);
        when(publicTemplate.getPublicTemplateTitle()).thenReturn("공용 제목");

        IndividualTemplate individualTemplate = mock(IndividualTemplate.class);
        when(individualTemplate.getIndividualTemplateId()).thenReturn(10);
        when(individualTemplate.getIndividualTemplateTitle()).thenReturn("개인 제목");

        Favorite publicFavorite = Favorite.builder().workspace(workspace).publicTemplate(publicTemplate).build();
        Favorite individualFavorite = Favorite.builder().workspace(workspace).individualTemplate(individualTemplate).build();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(favoriteRepository.findAllByWorkspaceOrderByFavoriteIdDesc(workspace)).thenReturn(List.of(publicFavorite, individualFavorite));

        // when
        List<FavoriteResponse> result = favoriteService.getFavoritesByWorkspace(workspaceId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

        // 공용 템플릿 즐겨찾기 내용 검증
        FavoriteResponse publicResponse = result.get(0);
        assertThat(publicResponse.getTemplateType()).isEqualTo("PUBLIC");
        assertThat(publicResponse.getTemplateId()).isEqualTo(publicTemplate.getPublicTemplateId());
        assertThat(publicResponse.getTemplateTitle()).isEqualTo(publicTemplate.getPublicTemplateTitle());

        // 개인 템플릿 즐겨찾기 내용 검증
        FavoriteResponse individualResponse = result.get(1);
        assertThat(individualResponse.getTemplateType()).isEqualTo("INDIVIDUAL");
        assertThat(individualResponse.getTemplateId()).isEqualTo(individualTemplate.getIndividualTemplateId());
        assertThat(individualResponse.getTemplateTitle()).isEqualTo(individualTemplate.getIndividualTemplateTitle());
    }

    @Test
    @DisplayName("성공(단위): 즐겨찾기 목록 페이징 조회 (public) - 반환된 내용 검증")
    void getFavorites_withPublicTemplateType_shouldReturnPage() {
        // given
        Integer workspaceId = 1;
        Pageable pageable = PageRequest.of(0, 10);
        Workspace workspace = mock(Workspace.class);

        PublicTemplate publicTemplate = mock(PublicTemplate.class);
        when(publicTemplate.getPublicTemplateId()).thenReturn(100);
        when(publicTemplate.getPublicTemplateTitle()).thenReturn("공용 제목");

        Favorite publicFavorite = Favorite.builder().workspace(workspace).publicTemplate(publicTemplate).build();
        Page<Favorite> mockPage = new PageImpl<>(List.of(publicFavorite), pageable, 1);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(favoriteRepository.findByWorkspaceAndPublicTemplateIsNotNull(workspace, pageable)).thenReturn(mockPage);

        // when
        Page<FavoriteResponse> result = favoriteService.getFavoritesByWorkspace(workspaceId, "public", pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        FavoriteResponse response = result.getContent().get(0);
        assertThat(response.getTemplateType()).isEqualTo("PUBLIC");
        assertThat(response.getTemplateId()).isEqualTo(publicTemplate.getPublicTemplateId());
        assertThat(response.getTemplateTitle()).isEqualTo(publicTemplate.getPublicTemplateTitle());
    }

    @Test
    @DisplayName("성공(단위): 즐겨찾기 목록 페이징 조회 (individual) - 반환된 내용 검증")
    void getFavorites_withIndividualTemplateType_shouldReturnPage() {
        // given
        Integer workspaceId = 1;
        Pageable pageable = PageRequest.of(0, 10);
        Workspace workspace = mock(Workspace.class);

        IndividualTemplate individualTemplate = mock(IndividualTemplate.class);
        when(individualTemplate.getIndividualTemplateId()).thenReturn(10);
        when(individualTemplate.getIndividualTemplateTitle()).thenReturn("개인 제목");

        Favorite individualFavorite = Favorite.builder().workspace(workspace).individualTemplate(individualTemplate).build();
        Page<Favorite> mockPage = new PageImpl<>(List.of(individualFavorite), pageable, 1);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(favoriteRepository.findByWorkspaceAndIndividualTemplateIsNotNull(workspace, pageable)).thenReturn(mockPage);

        // when
        Page<FavoriteResponse> result = favoriteService.getFavoritesByWorkspace(workspaceId, "individual", pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        FavoriteResponse response = result.getContent().get(0);
        assertThat(response.getTemplateType()).isEqualTo("INDIVIDUAL");
        assertThat(response.getTemplateId()).isEqualTo(individualTemplate.getIndividualTemplateId());
        assertThat(response.getTemplateTitle()).isEqualTo(individualTemplate.getIndividualTemplateTitle());
    }
}
