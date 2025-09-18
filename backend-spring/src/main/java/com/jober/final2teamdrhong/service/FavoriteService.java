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
import lombok.RequiredArgsConstructor;
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

}
