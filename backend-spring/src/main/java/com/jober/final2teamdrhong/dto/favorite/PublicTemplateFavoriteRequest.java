package com.jober.final2teamdrhong.dto.favorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class PublicTemplateFavoriteRequest {
    @NotNull(message = "워크스페이스 ID는 필수 입력값입니다.")
    private Integer workspaceId;

    @NotNull(message = "공용 템플릿 ID는 필수 입력값입니다.")
    private Integer publicTemplateId;
}
