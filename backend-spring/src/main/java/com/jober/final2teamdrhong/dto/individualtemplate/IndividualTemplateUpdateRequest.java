package com.jober.final2teamdrhong.dto.individualtemplate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IndividualTemplateUpdateRequest {
    private String individualTemplateTitle;
    private String individualTemplateContent;
    private String buttonTitle;

    @NotBlank
    private String chatAi;
    @NotBlank
    private String chatUser;
}
