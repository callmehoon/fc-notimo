package com.jober.final2teamdrhong.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateResponse;
import com.jober.final2teamdrhong.service.PublicTemplateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PublicTemplateController {
    private final PublicTemplateService publicTemplateService;

    /**
     * 공용 템플릿 목록을 조회합니다.
     * 삭제되지 않은 템플릿만 조회되며, 다양한 정렬 옵션과 페이징을 지원합니다.
     *
     * @param pageable 스프링이 자동 바인딩하는 페이징/정렬 정보
     *                 - 기본값: createdAt DESC, size=10
     *                 - 요청에서 page, size, sort 파라미터 사용 가능 (예: sort=shareCount,desc)
     * @return 페이징된 공용 템플릿 응답 객체
     */
    @GetMapping("/public-templates")
    public Page<PublicTemplateResponse> getPublicTemplates(
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable
    ) {
        return publicTemplateService.getTemplates(pageable);
    }
}
