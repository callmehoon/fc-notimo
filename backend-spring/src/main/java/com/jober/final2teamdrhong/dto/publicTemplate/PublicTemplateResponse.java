package com.jober.final2teamdrhong.dto.publicTemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Builder
@AllArgsConstructor
@Schema(name = "PublicTemplateResponse", description = "공용 템플릿 조회 응답")
public class PublicTemplateResponse {
    @Schema(description = "공용 템플릿 ID", example = "123")
    private Integer publicTemplateId;

    @Schema(description = "템플릿 제목", example = "회의 안내 템플릿")
    private String publicTemplateTitle;

    @Schema(description = "템플릿 내용", example = "안녕하세요. 회의 일정을 안내드립니다...")
    private String publicTemplateContent;

    @Schema(description = "버튼 제목", example = "회의 참석하기")
    private String buttonTitle;
}