package com.jober.final2teamdrhong.dto.publicTemplate;

import com.jober.final2teamdrhong.entity.PublicTemplate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PublicTemplateResponse", description = "공용 템플릿 조회/생성 응답")
public record PublicTemplateResponse(
    @Schema(description = "공용 템플릿 ID", example = "123")
    Integer publicTemplateId,

    @Schema(description = "템플릿 제목", example = "회의 안내 템플릿")
    String publicTemplateTitle,

    @Schema(description = "템플릿 내용", example = "안녕하세요. 회의 일정을 안내드립니다...")
    String publicTemplateContent,

    @Schema(description = "버튼 제목", example = "회의 참석하기")
    String buttonTitle
) {
    /**
     * PublicTemplate 엔티티를 PublicTemplateResponse DTO로 변환하는 정적 팩토리 메소드.
     *
     * @param entity 변환할 PublicTemplate 엔티티
     * @return 변환된 PublicTemplateResponse DTO
     */
    public static PublicTemplateResponse from(PublicTemplate entity) {
        return new PublicTemplateResponse(
            entity.getPublicTemplateId(),
            entity.getPublicTemplateTitle(),
            entity.getPublicTemplateContent(),
            entity.getButtonTitle()
        );
    }
}