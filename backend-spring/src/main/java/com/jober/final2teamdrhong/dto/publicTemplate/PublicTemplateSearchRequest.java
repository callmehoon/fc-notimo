package com.jober.final2teamdrhong.dto.publicTemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "공용 템플릿 검색 조건 DTO")
public class PublicTemplateSearchRequest {

    @Schema(description = "검색 키워드", example = "회의")
    private String keyword;

    @Schema(description = "검색 대상 필드", example = "ALL", defaultValue = "ALL")
    private SearchTarget searchTarget = SearchTarget.ALL;

    public enum SearchTarget {
        TITLE, CONTENT, ALL
    }

}
