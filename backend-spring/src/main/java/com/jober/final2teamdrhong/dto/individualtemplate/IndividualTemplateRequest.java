package com.jober.final2teamdrhong.dto.individualtemplate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class IndividualTemplateRequest {
    private String individualTemplateTitle;
    private String individualTemplateContent;
    private String buttonTitle;
    private Integer workspaceId;
}
