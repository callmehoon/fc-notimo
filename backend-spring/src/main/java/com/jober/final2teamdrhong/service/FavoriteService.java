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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
     *
     * @param request 즐겨찾기 생성을 위한 정보 (workspaceId, templateId)
     * @throws IllegalArgumentException 워크스페이스, 템플릿이 존재하지 않거나 이미 즐겨찾기로 등록되었을 경우 발생
     */
    @Transactional
    public void createIndividualTemplateFavorite(IndividualTemplateFavoriteRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스를 찾을 수 없습니다."));

        IndividualTemplate individualTemplate = individualTemplateRepository.findById(request.getIndividualTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("해당 개인 템플릿을 찾을 수 없습니다."));

        favoriteRepository.findByWorkspaceAndIndividualTemplate(workspace, individualTemplate)
                .ifPresent(f -> {
                    throw new IllegalArgumentException("이미 즐겨찾기된 개인 템플릿입니다.");
                });

        Favorite favorite = new Favorite(workspace, null, individualTemplate);
        favoriteRepository.save(favorite);
    }

    /**
     * 공용 템플릿을 즐겨찾기에 추가(create)
     *
     * @param request 즐겨찾기 생성을 위한 정보 (workspaceId, templateId)
     * @throws IllegalArgumentException 워크스페이스, 템플릿이 존재하지 않거나 이미 즐겨찾기로 등록되었을 경우 발생
     */
    @Transactional
    public void createPublicTemplateFavorite(PublicTemplateFavoriteRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스를 찾을 수 없습니다."));

        PublicTemplate publicTemplate = publicTemplateRepository.findById(request.getPublicTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("해당 공용 템플릿을 찾을 수 없습니다."));

        favoriteRepository.findByWorkspaceAndPublicTemplate(workspace, publicTemplate)
                .ifPresent(f -> {
                    throw new IllegalArgumentException("이미 즐겨찾기된 공용 템플릿입니다.");
                });

        Favorite favorite = new Favorite(workspace, publicTemplate, null);
        favoriteRepository.save(favorite);
    }


    // ========== read ==========

    /**
     * 특정 워크스페이스에 속한 모든 즐겨찾기 목록을 페이징 없이 조회합니다.
     *
     * @param workspaceId 조회의 기준이 되는 워크스페이스 ID
     * @return 해당 워크스페이스의 FavoriteResponse DTO 리스트
     */
    public List<FavoriteResponse> getFavoritesByWorkspace(Integer workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스를 찾을 수 없습니다."));

        List<Favorite> favorites = favoriteRepository.findAllByWorkspaceOrderByFavoriteIdDesc(workspace);
        return favorites.stream()
                .map(FavoriteResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 워크스페이스에 속한 즐겨찾기 목록을 템플릿 유형에 따라 페이징하여 조회합니다.
     *
     * @param workspaceId  조회의 기준이 되는 워크스페이스 ID
     * @param templateType 템플릿 유형 ("public" 또는 "individual")
     * @param pageable     페이징 정보
     * @return 해당 워크스페이스의 페이징된 FavoriteResponse DTO
     */
    public Page<FavoriteResponse> getFavoritesByWorkspace(Integer workspaceId, String templateType, Pageable pageable) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스를 찾을 수 없습니다."));

        Page<Favorite> favorites;
        if ("public".equalsIgnoreCase(templateType)) {
            favorites = favoriteRepository.findByWorkspaceAndPublicTemplateIsNotNull(workspace, pageable);
        } else if ("individual".equalsIgnoreCase(templateType)) {
            favorites = favoriteRepository.findByWorkspaceAndIndividualTemplateIsNotNull(workspace, pageable);
        } else {
            return Page.empty(pageable);
        }

        return favorites.map(FavoriteResponse::new);
    }


    // ========== delete ==========

    /**
     * 즐겨찾기를 삭제(delete)
     * @param favoriteId 삭제할 즐겨찾기 ID
     * @throws IllegalArgumentException 해당 즐겨찾기가 존재하지 않을 경우 발생
     */
    @Transactional
    public void deleteFavorite(Integer favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 즐겨찾기를 찾을 수 없습니다."));

        favoriteRepository.delete(favorite);
    }
}
