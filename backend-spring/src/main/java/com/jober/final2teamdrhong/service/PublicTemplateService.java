package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateCreateRequest;
import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplatePageableRequest;
import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateResponse;
import com.jober.final2teamdrhong.entity.Favorite;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.repository.FavoriteRepository;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;
import com.jober.final2teamdrhong.repository.PublicTemplateSpecification;
import com.jober.final2teamdrhong.service.validator.WorkspaceValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicTemplateService {

    private final PublicTemplateRepository publicTemplateRepository;
    private final IndividualTemplateRepository individualTemplateRepository;
    private final WorkspaceValidator workspaceValidator;
    private final FavoriteRepository favoriteRepository;
    
    /**
     * 삭제되지 않은 공용 템플릿 목록을 페이징하여 조회한다.
     *
     * @param request 요청으로부터 바인딩된 페이징/정렬/검색 정보
     * @return 페이징된 PublicTemplateResponse 목록
     */
    @Transactional(readOnly = true)
    public Page<PublicTemplateResponse> getTemplates(PublicTemplatePageableRequest request) {
        Pageable pageable = request.toPageable();
        Specification<PublicTemplate> spec = PublicTemplateSpecification.withSearch(request.getSearch());
        
        return publicTemplateRepository.findAll(spec, pageable)
            .map(PublicTemplateResponse::from);
    }   

    /**
     * 개인 템플릿을 기반으로 공용 템플릿을 생성하고, 생성된 공용 템플릿 정보를 반환한다.
     *
     * @param request 개인 템플릿 ID를 담은 요청 DTO
     * @return 생성된 공용 템플릿 정보 {@link PublicTemplateResponse}
     * @throws IllegalArgumentException 요청한 개인 템플릿이 존재하지 않거나, 해당 개인 템플릿의 워크스페이스가 현재 사용자의 소유가 아닐 경우 발생
     */
    public PublicTemplateResponse createPublicTemplate(PublicTemplateCreateRequest request, Integer userId) {
        IndividualTemplate individualTemplate = individualTemplateRepository.findByIdOrThrow(request.individualTemplateId());

        // IndividualTemplate의 Workspace가 현재 User의 소유인지 검증
        workspaceValidator.validateAndGetWorkspace(individualTemplate.getWorkspace().getWorkspaceId(), userId);

        // 개인 템플릿 값을 복사해서 PublicTemplate 생성
        PublicTemplate publicTemplate = PublicTemplate.builder()
            .publicTemplateTitle(individualTemplate.getIndividualTemplateTitle())
            .publicTemplateContent(individualTemplate.getIndividualTemplateContent())
            .buttonTitle(individualTemplate.getButtonTitle())
            .build();

        PublicTemplate savedPublicTemplate = publicTemplateRepository.save(publicTemplate);

        return PublicTemplateResponse.from(savedPublicTemplate);
    }

    /**
     * 공용 템플릿을 소프트 삭제 처리한다.
     *
     * @param publicTemplateId 삭제할 공용 템플릿의 ID
     * @throws IllegalArgumentException 지정한 ID의 템플릿이 존재하지 않는 경우 발생
     */
    public void deletePublicTemplate(Integer publicTemplateId) {
        PublicTemplate publicTemplate = publicTemplateRepository.findByIdOrThrow(publicTemplateId);

        List<Favorite> favorites = favoriteRepository.findAllByPublicTemplate_publicTemplateId(publicTemplateId);

        publicTemplate.deleteFavorites();
        favoriteRepository.deleteAll(favorites);

        publicTemplate.softDelete();
    }
} 