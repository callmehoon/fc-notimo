package com.jober.final2teamdrhong.dto.individualtemplate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class IndividualTemplateResponse {
    private Integer individualTemplateId;
    private String individualTemplateTitle;     // null 가능
    private String individualTemplateContent;   // null 가능
    private String buttonTitle;                 // null 가능
    private Integer workspaceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
