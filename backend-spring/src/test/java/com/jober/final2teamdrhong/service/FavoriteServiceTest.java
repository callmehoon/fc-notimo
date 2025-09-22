package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.favorite.FavoriteResponse;
import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.entity.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class  FavoriteServiceTest {

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

    private JwtClaims mockJwtClaims;
    private Workspace mockWorkspace;
    private IndividualTemplate mockIndividualTemplate;
    private PublicTemplate mockPublicTemplate;

    @BeforeEach
    void setUp() {
        mockJwtClaims = JwtClaims.builder().userId(1).build();
        User mockUser = User.builder().userId(1).build();
        mockWorkspace = Workspace.builder()
                .workspaceId(1)
                .user(mockUser)
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("테스트 대표")
                .representerPhoneNumber("010-1234-5678")
                .companyName("테스트 회사")
                .build();
        mockIndividualTemplate = IndividualTemplate.builder().individualTemplateId(10).individualTemplateTitle("개인 템플릿").build();
        mockPublicTemplate = PublicTemplate.builder().publicTemplateId(100).publicTemplateTitle("공용 템플릿").build();
    }

    @Test
    @DisplayName("성공(서비스): 개인 템플릿 즐겨찾기 생성")
    void createIndividualTemplateFavorite_Success() {
        // given
        IndividualTemplateFavoriteRequest request = new IndividualTemplateFavoriteRequest(1, 10);
        Favorite savedFavorite = Favorite.builder().favoriteId(99).workspace(mockWorkspace).individualTemplate(mockIndividualTemplate).build();

        when(workspaceRepository.findByIdOrThrow(1, 1)).thenReturn(mockWorkspace);
        when(individualTemplateRepository.findByIdOrThrow(10)).thenReturn(mockIndividualTemplate);
        doNothing().when(favoriteRepository).validateIndividualTemplateNotExists(mockWorkspace, mockIndividualTemplate);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(savedFavorite);

        // when
        FavoriteResponse response = favoriteService.createIndividualTemplateFavorite(mockJwtClaims, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFavoriteId()).isEqualTo(99);
        assertThat(response.getTemplateType()).isEqualTo("INDIVIDUAL");
        verify(workspaceRepository).findByIdOrThrow(1, 1);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("실패(서비스): 이미 즐겨찾기한 개인 템플릿 생성 시 예외 발생")
    void createIndividualTemplateFavorite_Fail_AlreadyExists() {
        // given
        IndividualTemplateFavoriteRequest request = new IndividualTemplateFavoriteRequest(1, 10);
        when(workspaceRepository.findByIdOrThrow(1, 1)).thenReturn(mockWorkspace);
        when(individualTemplateRepository.findByIdOrThrow(10)).thenReturn(mockIndividualTemplate);
        doThrow(new IllegalArgumentException("이미 즐겨찾기된 개인 템플릿입니다."))
                .when(favoriteRepository).validateIndividualTemplateNotExists(mockWorkspace, mockIndividualTemplate);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.createIndividualTemplateFavorite(mockJwtClaims, request);
        });
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("성공(서비스): 공용 템플릿 즐겨찾기 생성")
    void createPublicTemplateFavorite_Success() {
        // given
        PublicTemplateFavoriteRequest request = new PublicTemplateFavoriteRequest(1, 100);
        Favorite savedFavorite = Favorite.builder().favoriteId(101).workspace(mockWorkspace).publicTemplate(mockPublicTemplate).build();

        when(workspaceRepository.findByIdOrThrow(1, 1)).thenReturn(mockWorkspace);
        when(publicTemplateRepository.findByIdOrThrow(100)).thenReturn(mockPublicTemplate);
        doNothing().when(favoriteRepository).validatePublicTemplateNotExists(mockWorkspace, mockPublicTemplate);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(savedFavorite);

        // when
        FavoriteResponse response = favoriteService.createPublicTemplateFavorite(mockJwtClaims, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFavoriteId()).isEqualTo(101);
        assertThat(response.getTemplateType()).isEqualTo("PUBLIC");
        verify(workspaceRepository).findByIdOrThrow(1, 1);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("실패(서비스): 이미 즐겨찾기한 공용 템플릿 생성 시 예외 발생")
    void createPublicTemplateFavorite_Fail_AlreadyExists() {
        // given
        PublicTemplateFavoriteRequest request = new PublicTemplateFavoriteRequest(1, 100);
        when(workspaceRepository.findByIdOrThrow(1, 1)).thenReturn(mockWorkspace);
        when(publicTemplateRepository.findByIdOrThrow(100)).thenReturn(mockPublicTemplate);
        doThrow(new IllegalArgumentException("이미 즐겨찾기된 공용 템플릿입니다."))
                .when(favoriteRepository).validatePublicTemplateNotExists(mockWorkspace, mockPublicTemplate);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.createPublicTemplateFavorite(mockJwtClaims, request);
        });
        verify(favoriteRepository, never()).save(any(Favorite.class));
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
}
