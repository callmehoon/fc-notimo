package com.jober.final2teamdrhong.controller;

import org.springframework.data.domain.Page;
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
     * @param sortBy 정렬 기준 (share: 공유순, view: 조회순, recent: 최신순, title: 제목 가나다순)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기 (한 페이지당 항목 수)
     * @return 페이징된 공용 템플릿 응답 객체
     */
    @GetMapping("/public-templates")
    public Page<PublicTemplateResponse> getPublicTemplates(
        @RequestParam(defaultValue = "recent") String sortBy,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer size
    ) {
        return publicTemplateService.getTemplates(sortBy, page, size);
    }
}
