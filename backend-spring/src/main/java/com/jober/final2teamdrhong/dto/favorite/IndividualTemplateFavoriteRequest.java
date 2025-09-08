package com.jober.final2teamdrhong.dto.favorite;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IndividualTemplateFavoriteRequest {
    @NotNull(message = "워크스페이스 ID는 필수 입력값입니다.")
    private Integer workspaceId;

    @NotNull(message = "개인 템플릿 ID는 필수 입력값입니다.")
    private Integer individualTemplateId;
}
