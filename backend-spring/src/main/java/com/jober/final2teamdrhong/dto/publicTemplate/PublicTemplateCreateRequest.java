package com.jober.final2teamdrhong.dto.publicTemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "PublicTemplateCreateRequest", description = "공용 템플릿 생성 요청 DTO")
public record PublicTemplateCreateRequest(
    @Schema(description = "공유할 개인 템플릿 ID", example = "123", required = true)
    @NotNull(message = "개인 템플릿 ID는 필수입니다.")
    Integer individualTemplateId
) {}