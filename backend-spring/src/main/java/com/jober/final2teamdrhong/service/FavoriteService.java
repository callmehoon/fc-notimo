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
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스를 찾을 수 없습니다."));

        IndividualTemplate individualTemplate = individualTemplateRepository.findById(request.getIndividualTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("해당 템플릿을 찾을 수 없습니다."));

        favoriteRepository.findByWorkspaceAndIndividualTemplate(workspace, individualTemplate)
                .ifPresent(f -> {throw new IllegalArgumentException("이미 즐겨찾기된 템플릿입니다.");});

        Favorite favorite = new Favorite(workspace, null, individualTemplate);
        favoriteRepository.save(favorite);
    }

    /**
     * 공용 템플릿을 즐겨찾기에 추가(create)
     * @param request 즐겨찾기 생성을 위한 정보 (workspaceId, templateId)
     * @throws IllegalArgumentException 워크스페이스, 템플릿이 존재하지 않거나 이미 즐겨찾기로 등록되었을 경우 발생
     */
    @Transactional
    public void createPublicTemplateFavorite(PublicTemplateFavoriteRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스를 찾을 수 없습니다."));

        PublicTemplate publicTemplate = publicTemplateRepository.findById(request.getPublicTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("해당 템플릿을 찾을 수 없습니다."));

        favoriteRepository.findByWorkspaceAndPublicTemplate(workspace, publicTemplate)
                .ifPresent(f -> {throw new IllegalArgumentException("이미 즐겨찾기된 템플릿입니다.");});

        Favorite favorite = new Favorite(workspace, publicTemplate, null);
        favoriteRepository.save(favorite);
    }

}
