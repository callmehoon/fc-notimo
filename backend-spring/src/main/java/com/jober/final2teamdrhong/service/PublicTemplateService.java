package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateCreateRequest;
import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateResponse;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicTemplateService {

    private final PublicTemplateRepository publicTemplateRepository;
    private final IndividualTemplateRepository individualTemplateRepository;
    
    /**
     * 삭제되지 않은 공용 템플릿 목록을 페이징하여 조회합니다.
     *
     * @param pageable 요청으로부터 바인딩된 페이징/정렬 정보
     * @return 페이징된 PublicTemplateResponse 목록
     */
    @Transactional(readOnly = true)
    public Page<PublicTemplateResponse> getTemplates(Pageable pageable) {
        return publicTemplateRepository.findAll(pageable)
            .map(this::toResponse);
    }   

    /**
     * PublicTemplate 엔티티를 PublicTemplateResponse DTO로 변환합니다.
     *
     * @param entity 변환할 PublicTemplate 엔티티
     * @return 변환된 PublicTemplateResponse DTO
     */
    private PublicTemplateResponse toResponse(PublicTemplate entity) {
        return new PublicTemplateResponse(
            entity.getPublicTemplateId(),
            entity.getPublicTemplateTitle(),
            entity.getPublicTemplateContent(),
            entity.getButtonTitle()
        );
    }

    /**
     * 개인 템플릿을 기반으로 공용 템플릿을 생성하고, 생성된 공용 템플릿 정보를 반환한다.
     *
     * @param request 개인 템플릿 ID를 담은 요청 DTO
     * @return 생성된 공용 템플릿 정보 {@link PublicTemplateResponse}
     * @throws EntityNotFoundException 요청한 개인 템플릿이 존재하지 않을 경우
     */
    public PublicTemplateResponse createPublicTemplate(PublicTemplateCreateRequest request) {
        IndividualTemplate individualTemplate = individualTemplateRepository.findById(request.individualTemplateId())
            .orElseThrow(() -> new EntityNotFoundException("IndividualTemplate not found. id=" + request.individualTemplateId()));

        // 개인 템플릿 값을 복사해서 PublicTemplate 생성
        PublicTemplate publicTemplate = PublicTemplate.builder()
            .publicTemplateTitle(individualTemplate.getIndividualTemplateTitle())
            .publicTemplateContent(individualTemplate.getIndividualTemplateContent())
            .buttonTitle(individualTemplate.getButtonTitle())
            .build();

        PublicTemplate savedPublicTemplate = publicTemplateRepository.save(publicTemplate);

        return toResponse(savedPublicTemplate);
    }
} 