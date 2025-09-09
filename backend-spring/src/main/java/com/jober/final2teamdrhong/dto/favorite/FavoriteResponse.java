package com.jober.final2teamdrhong.dto.favorite;

import com.jober.final2teamdrhong.entity.Favorite;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import lombok.Getter;

public class FavoriteResponse {

    @Getter
    private final Integer favoriteId;
    @Getter
    private final String templateType;
    @Getter
    private final Integer templateId;
    @Getter
    private final String templateTitle;
    @Getter
    private final String templateContent;
    @Getter
    private final String buttonTitle;
    @Getter
    private final Integer viewCount;
    @Getter
    private final Integer shareCount;

    public FavoriteResponse(Favorite favorite) {
        this.favoriteId = favorite.getFavoriteId();

        if (favorite.getPublicTemplate() != null) {
            PublicTemplate pt = favorite.getPublicTemplate();
            this.templateType = "PUBLIC";
            this.templateId = pt.getPublicTemplateId();
            this.templateTitle = pt.getPublicTemplateTitle();
            this.templateContent = pt.getPublicTemplateContent();
            this.buttonTitle = pt.getButtonTitle();
            this.viewCount = pt.getViewCount();
            this.shareCount = pt.getShareCount();
        } else {
            IndividualTemplate it = favorite.getIndividualTemplate();
            this.templateType = "INDIVIDUAL";
            this.templateId = it.getIndividualTemplateId();
            this.templateTitle = it.getIndividualTemplateTitle();
            this.templateContent = it.getIndividualTemplateContent();
            this.buttonTitle = it.getButtonTitle();
            this.viewCount = null;
            this.shareCount = null;
        }
    }
}