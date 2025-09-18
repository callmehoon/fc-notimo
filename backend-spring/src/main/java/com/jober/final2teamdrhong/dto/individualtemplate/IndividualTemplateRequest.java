package com.jober.final2teamdrhong.dto.individualtemplate;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IndividualTemplateRequest {
    private String individualTemplateTitle;
    private String individualTemplateContent;
    private String buttonTitle;
    private Integer workspaceId;
}
