package com.jober.final2teamdrhong.dto;

import com.jober.final2teamdrhong.entity.Favorite;
import lombok.Getter;

@Getter
public class IndividualTemplateFavoriteResponse {
    private final Integer favoriteId;
    private final Integer templateId;
    private final String individualTemplateTitle;
    private final String individualTemplateContent;

    public IndividualTemplateFavoriteResponse(Favorite favorite) {
        this.favoriteId = favorite.getFavoriteId();
        this.templateId = favorite.getIndividualTemplate().getIndividualTemplateId();
        this.individualTemplateTitle = favorite.getIndividualTemplate().getIndividualTemplateTitle();
        this.individualTemplateContent = favorite.getIndividualTemplate().getIndividualTemplateContent();
    }

}
