package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateResponse;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicTemplateService {

    private final PublicTemplateRepository publicTemplateRepository;

    /**
     * 삭제되지 않은 공용 템플릿 목록을 페이징하여 조회합니다.
     *
     * @param sortBy 정렬 기준 (share, view, recent, title 중 하나, null 허용)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 PublicTemplateResponse 목록
     */
    @Transactional(readOnly = true)
    public Page<PublicTemplateResponse> getTemplates(String sortBy, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, getSortOption(sortBy));

        return publicTemplateRepository.findAllByIsDeletedFalse(pageable)
            .map(this::toResponse);
    }

    /**
     * 정렬 기준에 따른 Sort 객체를 생성합니다.
     *
     * @param sortBy 정렬 기준 문자열
     *               - "share": 공유수 내림차순
     *               - "view": 조회수 내림차순  
     *               - "recent": 생성일시 내림차순 (최신순)
     *               - "title": 제목 오름차순 (가나다순)
     *               - 기타/null: 기본값으로 최신순 적용
     * @return 해당 정렬 기준의 Sort 객체
     */
    private Sort getSortOption(String sortBy) {
        if (sortBy == null) {
            sortBy = "recent";
        }
        switch (sortBy) {
            case "share":
                return Sort.by("shareCount").descending();
            case "view":
                return Sort.by("viewCount").descending();
            case "recent":
                return Sort.by("createdAt").descending();
            case "title":
                return Sort.by("publicTemplateTitle").ascending();
            default:
                return Sort.by("createdAt").descending();   // 기본값: 최신순
        }
    }

    /**
     * PublicTemplate 엔티티를 PublicTemplateResponse DTO로 변환합니다.
     *
     * @param entity 변환할 PublicTemplate 엔티티
     * @return 변환된 PublicTemplateResponse DTO
     */
    private PublicTemplateResponse toResponse(PublicTemplate entity) {
        return PublicTemplateResponse.builder()
            .publicTemplateId(entity.getPublicTemplateId())
            .publicTemplateTitle(entity.getPublicTemplateTitle())
            .publicTemplateContent(entity.getPublicTemplateContent())
            .shareCount(entity.getShareCount())
            .viewCount(entity.getViewCount())
            .createdAt(entity.getCreatedAt())
            .build();
    }
} 