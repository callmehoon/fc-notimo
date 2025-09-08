package com.jober.final2teamdrhong.dto.favorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PublicTemplateFavoriteRequest {
    private Integer workspaceId;
    private Integer publicTemplateId;
}
