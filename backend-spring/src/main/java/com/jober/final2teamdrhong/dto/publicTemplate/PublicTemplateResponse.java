package com.jober.final2teamdrhong.dto.publicTemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PublicTemplateResponse {
    private Integer publicTemplateId;
    private String publicTemplateTitle;
    private String publicTemplateContent;
    private String buttonTitle;
}