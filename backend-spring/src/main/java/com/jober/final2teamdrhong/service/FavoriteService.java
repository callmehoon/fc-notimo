package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.favorite.FavoritePageRequest;
import com.jober.final2teamdrhong.dto.favorite.FavoriteResponse;
import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.entity.Favorite;
import com.jober.final2teamdrhong.entity.Favorite.TemplateType;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.FavoriteRepository;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final WorkspaceRepository workspaceRepository;
    private final IndividualTemplateRepository individualTemplateRepository;
    private final PublicTemplateRepository publicTemplateRepository;

    // ========== create ==========
    /**
     * 개인 템플릿을 즐겨찾기에 추가(create)
     * @param request 즐겨찾기 생성을 위한 정보 (workspaceId, templateId)
     * @throws IllegalArgumentException 워크스페이스, 템플릿이 존재하지 않거나 이미 즐겨찾기로 등록되었을 경우 발생
     */
    @Transactional
    public void createIndividualTemplateFavorite(IndividualTemplateFavoriteRequest request) {
        Workspace workspace = workspaceRepository.findByIdOrThrow(request.getWorkspaceId());

        IndividualTemplate individualTemplate = individualTemplateRepository.findByIdOrThrow(request.getIndividualTemplateId());

        favoriteRepository.validateIndividualTemplateNotExists(workspace, individualTemplate);

        Favorite favorite = Favorite.builder()
                .workspace(workspace)
                .individualTemplate(individualTemplate)
                .build();
        favoriteRepository.save(favorite);
    }

    /**
     * 공용 템플릿을 즐겨찾기에 추가(create)
     * @param request 즐겨찾기 생성을 위한 정보 (workspaceId, templateId)
     * @throws IllegalArgumentException 워크스페이스, 템플릿이 존재하지 않거나 이미 즐겨찾기로 등록되었을 경우 발생
     */
    @Transactional
    public void createPublicTemplateFavorite(PublicTemplateFavoriteRequest request) {
        Workspace workspace = workspaceRepository.findByIdOrThrow(request.getWorkspaceId());

        PublicTemplate publicTemplate = publicTemplateRepository.findByIdOrThrow(request.getPublicTemplateId());

        favoriteRepository.validatePublicTemplateNotExists(workspace, publicTemplate);

        Favorite favorite = Favorite.builder()
                .workspace(workspace)
                .publicTemplate(publicTemplate)
                .build();
        favoriteRepository.save(favorite);
    }


    // ========== read ==========
    /**
     * 특정 워크스페이스에 속한 모든 즐겨찾기 목록을 페이징 없이 조회합니다.
     *
     * @param workspaceId 조회의 기준이 되는 워크스페이스 ID
     * @return 해당 워크스페이스의 FavoriteResponse DTO 리스트
     */
    public Page<FavoriteResponse> getFavoritesByWorkspace(Integer workspaceId, TemplateType templateType, FavoritePageRequest favoritePageRequest) {
        Workspace workspace = workspaceRepository.findByIdOrThrow(workspaceId);
        Pageable pageable = PageRequest.of(favoritePageRequest.getPage(), favoritePageRequest.getSize(), Sort.by(Sort.Direction.DESC, "favoriteId"));

        Page<Favorite> favorites = favoriteRepository.findFavorites(workspace, templateType, pageable);
        return favorites.map(this::convertToFavoriteResponse);
    }

    // getFavoritesByWorkspace 메서드를 위한 Favorite 엔티티 -> DTO 변환 메서드
    private FavoriteResponse convertToFavoriteResponse(Favorite favorite) {
        if (favorite.getPublicTemplate() != null) {
            return FavoriteResponse.fromPublicTemplate(favorite);
        }
        return FavoriteResponse.fromIndividualTemplate(favorite);
    }

}
