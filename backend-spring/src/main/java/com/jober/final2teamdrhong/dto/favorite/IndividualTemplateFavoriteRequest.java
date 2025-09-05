package com.jober.final2teamdrhong.dto.favorite;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IndividualTemplateFavoriteRequest {
    private Integer workspaceId;
    private Integer individualTemplateId;

    public IndividualTemplateFavoriteRequest(Integer workspaceId, Integer individualTemplateId) {
        this.workspaceId = workspaceId;
        this.individualTemplateId = individualTemplateId;
    }
}
