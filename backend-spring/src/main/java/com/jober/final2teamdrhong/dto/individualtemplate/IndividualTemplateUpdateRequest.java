package com.jober.final2teamdrhong.dto.individualtemplate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IndividualTemplateUpdateRequest {
    private String individualTemplateTitle;
    private String individualTemplateContent;
    private String buttonTitle;
    private String chatAi;
    private String chatUser;
}
